package com.plog.domain.post.service;

import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.PostException;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public Long createPost(String title, String content) {
        String plainText = extractPlainText(content);
        String summary = extractSummary(plainText);

        Post post = Post.builder()
                .title(title)
                .content(content)
                .summary(summary)
                .status(PostStatus.PUBLISHED)
                .build();

        return postRepository.save(post).getId();
    }

    @Override
    @Transactional
    public PostInfoRes getPostDetail(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[PostServiceImpl#getPostDetail] can't find post by id", "존재하지 않는 게시물입니다."));

        post.incrementViewCount();
        return PostInfoRes.from(post);
    }

    @Override
    public List<PostInfoRes> getPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(PostInfoRes::from)
                .collect(Collectors.toList());
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
}