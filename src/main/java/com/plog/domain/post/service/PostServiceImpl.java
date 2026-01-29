package com.plog.domain.post.service;

import com.plog.domain.comment.constant.CommentConstants;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.hashtag.entity.PostHashTag;
import com.plog.domain.hashtag.repository.HashTagRepository;
import com.plog.domain.hashtag.repository.PostHashTagRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.dto.PostCreateReq;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.dto.PostListRes;
import com.plog.domain.post.dto.PostUpdateReq;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.exception.exceptions.PostException;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link PostService} 인터페이스의 기본 구현체입니다.
 * <p>
 * {@code @Service}와 {@code @Transactional}을 통해 스프링 빈으로 관리되며,
 * CommonMark 라이브러리를 이용한 마크다운 파싱 로직을 포함합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * CommonMark v0.21.0 (Parser, TextContentRenderer)
 *
 * @author MintyU
 * @since 2026-01-19
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    /** 요약본 생성을 위한 최대 글자 수 기준입니다. */
    private static final int MAX_SUMMARY_LENGTH = 150;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostHashTagRepository postHashTagRepository;
    private final HashTagRepository hashTagRepository;

    @Override
    @Transactional
    public Long createPost(Long memberId, PostCreateReq req) {
        Member member = memberRepository.getReferenceById(memberId);
        String plainText = extractPlainText(req.content());
        String summary = extractSummary(plainText);

        Post post = Post.builder()
                .title(req.title())
                .content(req.content())
                .summary(summary)
                .member(member)
                .status(PostStatus.PUBLISHED)
                .thumbnail(req.thumbnail())
                .build();
        post = postRepository.save(post);

        applyTags(post, req.hashtags());

        return post.getId();
    }

    @Override
    @Transactional
    public PostInfoRes getPostDetail(Long id, int pageNumber) {
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[PostServiceImpl#getPostDetail] can't find post by id", "존재하지 않는 게시물입니다."));

        post.incrementViewCount();

        Pageable pageable = PageRequest.of(
                pageNumber,
                CommentConstants.COMMENT_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, CommentConstants.DEFAULT_SORT_FIELD)
        );

        Slice<Comment> comments = commentRepository.findCommentsWithMemberAndImageByPostId(id, pageable);

        Slice<CommentInfoRes> commentResSlice = comments.map(this::convertToCommentInfoRes);

        return PostInfoRes.from(post, commentResSlice);
    }

    private CommentInfoRes convertToCommentInfoRes(Comment comment) {

        Pageable replyPageable = PageRequest.of(
                0,
                CommentConstants.REPLY_PAGE_SIZE,
                Sort.by("createDate").ascending()
        );

        Slice<Comment> replySlice = commentRepository.findRepliesWithMemberAndImageByParentId(comment.getId(), replyPageable);

        return new CommentInfoRes(comment, replySlice.map(ReplyInfoRes::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostListRes> getPosts(Pageable pageable) {
        return postRepository.findAllWithMember(pageable)
                .map(PostListRes::from);
    }

    @Override
    @Transactional
    public void updatePost(Long memberId, Long postId, PostUpdateReq req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[PostServiceImpl#updatePost] can't find post", "존재하지 않는 게시물입니다."));

        if (!post.getMember().getId().equals(memberId)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL,
                    "[PostServiceImpl#updatePost] user " + memberId + " is not the owner of post " + postId,
                    "해당 게시물을 수정할 권한이 없습니다.");
        }

        String plainText = extractPlainText(req.content());
        String summary = extractSummary(plainText);

        post.update(req.title(), req.content(), summary, req.thumbnail());

        postHashTagRepository.deleteAllByPostId(postId);

        applyTags(post, req.hashtags()); // 공통 로직 호출
    }

    @Override
    @Transactional
    public void deletePost(Long memberId, Long postId) {
        // 1. 게시물 존재 여부 확인 및 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[PostServiceImpl#deletePost] can't find post by id", "존재하지 않는 게시물입니다."));

        // 2. 작성자 본인 확인 (권한 체크)
        if (!post.getMember().getId().equals(memberId)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL,
                    "[PostServiceImpl#deletePost] user " + memberId + " is not the owner of post " + postId,
                    "해당 게시물을 삭제할 권한이 없습니다.");
        }

        // 3. 제약 조건 위반을 방지하기 위해 자식(대댓글)부터 삭제
        commentRepository.deleteRepliesByPostId(postId);
        // 4. 그 후 부모 댓글 삭제
        commentRepository.deleteParentsByPostId(postId);
        // 5. 연결된 해시태그 정보 삭제
        postHashTagRepository.deleteAllByPostId(postId);
        // 6. 게시물 삭제
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PostInfoRes> getPostsByMember(Long memberId, Pageable pageable) {
        Slice<Post> postSlice = postRepository.findAllByMemberId(memberId, pageable);

        return postSlice.map(PostInfoRes::from);
    }

    /**
     * 마크다운 텍스트에서 특수기호를 제거하고 순수 텍스트만 추출합니다.
     * * @param markdown 마크다운 원문
     * @return 추출된 순수 텍스트
     */
    private String extractPlainText(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        return renderer.render(document);
    }

    /**
     * 순수 텍스트에서 앞부분 150자만 추출하여 요약글을 생성하며,
     * 글자 수를 초과할 경우 "..."을 접미사로 추가합니다.
     * * @param plainText 추출된 순수 텍스트
     * @return 가공된 요약본 문자열
     */
    private String extractSummary(String plainText) {
        if (plainText.length() <= MAX_SUMMARY_LENGTH) {
            return plainText;
        }
        return plainText.substring(0, MAX_SUMMARY_LENGTH) + "...";
    }

    private void applyTags(Post post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        for (String rawName : tagNames) {
            String normalizedName = normalizeTag(rawName);

            HashTag hashTag = hashTagRepository.findByName(normalizedName)
                    .orElseGet(() -> hashTagRepository.save(new HashTag(normalizedName)));

            if (!postHashTagRepository.existsByPostIdAndHashTagId(post.getId(), hashTag.getId())) {
                PostHashTag postHashTag = PostHashTag.builder()
                        .post(post)
                        .hashTag(hashTag)
                        .displayName(rawName)
                        .build();

                postHashTagRepository.save(postHashTag);
            }
        }
    }

    private String normalizeTag(String name) {
        return name.trim().toLowerCase().replace(" ", "_");
    }
}