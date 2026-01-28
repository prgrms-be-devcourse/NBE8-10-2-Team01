package com.plog.domain.comment.service;

import com.plog.domain.comment.constant.CommentConstants;
import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.errorCode.CommentErrorCode;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.exception.exceptions.CommentException;
import com.plog.global.exception.exceptions.PostException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Long createComment(Long postId, Long memberId, CommentCreateReq req){

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[CommentServiceImpl#getPostDetail] can't find post by id", "존재하지 않는 게시물입니다."));

        Comment parentComment = null;

        String content = req.content();
        Long parentCommentId = req.parentCommentId();

        if(parentCommentId != null){
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CommentException(
                            CommentErrorCode.COMMENT_NOT_FOUND,
                            "[CommentService#createComment] parent comment not found. parentCommentId=" + parentCommentId,
                            "부모 댓글이 존재하지 않습니다."
                    ));

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new CommentException(
                        CommentErrorCode.INVALID_PARENT_COMMENT,
                        "[CommentService#createComment] post mismatch. parentComment.postId=" + parentComment.getPost().getId() + ", request.postId=" + postId,
                        "해당 게시물의 댓글이 아닙니다."
                );
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(content)
                .parent(parentComment)
                .build();

        return commentRepository.save(comment).getId();
    }

    //부모 댓글과 prefetch 자식 댓글을 하나의 DTO로 바꾸는 로직
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
    public Slice<CommentInfoRes> getCommentsByPostId(Long postId, int pageNumber) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(
                        PostErrorCode.POST_NOT_FOUND,
                        "[CommentService#getCommentsByPostId] can't find post by id : " + postId,
                        "존재하지 않는 게시물입니다."
                ));

        Pageable pageable = PageRequest.of(
                pageNumber,
                CommentConstants.COMMENT_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, CommentConstants.DEFAULT_SORT_FIELD)
        );

        Slice<Comment> comments = commentRepository.findCommentsWithMemberAndImageByPostId(postId, pageable);

        return comments.map(this::convertToCommentInfoRes);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ReplyInfoRes> getRepliesByCommentId(Long commentId, int pageNumber) {

        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND,
                        "[CommentService#getRepliesByCommentId] 부모 댓글이 존재하지 않음: " + commentId,
                        "존재하지 않는 댓글입니다."));

        Pageable pageable = PageRequest.of(
                pageNumber,
                CommentConstants.REPLY_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, CommentConstants.DEFAULT_SORT_FIELD)
        );

        Slice<Comment> replies = commentRepository.findRepliesWithMemberAndImageByParentId(commentId, pageable);

        return replies.map(ReplyInfoRes::new);
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, Long memberId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(
                        CommentErrorCode.COMMENT_NOT_FOUND,
                        "[CommentService#updateComment] can't find comment by id : " + commentId,
                        "존재하지 않는 댓글입니다."
                ));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL,
                    "[CommentService#updateComment] authorization failed. requesterId=" + memberId,
                    "수정 권한이 없습니다.");
        }

        comment.modify(content);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(
                        CommentErrorCode.COMMENT_NOT_FOUND,
                        "[CommentService#deleteComment] can't find comment by id : " + commentId,
                        "존재하지 않는 댓글입니다."
                ));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL,
                    "[CommentService#deleteComment] authorization failed. requesterId=" + memberId,
                    "삭제 권한이 없습니다.");
        }

        if(comment.isDeleted()){
            return;
        }

        boolean hasChildComments = commentRepository.existsByParent(comment);

        if(hasChildComments){
            comment.softDelete();
        }else{
            commentRepository.delete(comment);
        }
    }
}
