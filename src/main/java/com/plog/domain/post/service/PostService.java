package com.plog.domain.post.service;

import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.PostException;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시물 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 게시물의 생성, 수정, 조회, 삭제(CRUD) 기능을 제공하며,
 * 저장 시 마크다운 본문을 분석하여 검색용 순수 텍스트와 요약본을 자동으로 생성합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostService(PostRepository postRepository)} <br>
 * 의존성 주입을 통해 레포지토리를 초기화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service} 어노테이션을 통해 스프링 컨테이너에 의해 싱글톤 빈으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * CommonMark 라이브러리를 사용하여 마크다운 텍스트 파싱을 처리합니다.
 *
 * @author MintyU
 * @since 2026-01-16
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    /**
     * 새로운 게시물을 작성하고 저장합니다.
     * * @param title 게시물 제목
     * @param content 마크다운 형식의 본문
     * @return 저장된 게시물의 ID
     */
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

    @Transactional
    public Post getPostDetail(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND,
                        "[PostService#getPostDetail] can't find post by id", "존재하지 않는 게시물입니다."));
        post.incrementViewCount();
        return post;
    }

    /**
     * 마크다운 텍스트에서 특수기호를 제거하고 순수 텍스트만 추출합니다.
     */
    private String extractPlainText(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        return renderer.render(document);
    }

    /**
     * 순수 텍스트에서 앞부분 150자만 추출하여 요약글을 만듭니다.
     */
    private String extractSummary(String plainText) {
        if (plainText.length() <= 150) {
            return plainText;
        }
        return plainText.substring(0, 150) + "...";
    }
}
