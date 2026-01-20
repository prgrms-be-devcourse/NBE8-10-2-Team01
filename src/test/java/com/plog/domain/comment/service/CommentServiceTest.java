package com.plog.domain.comment.service;

import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author njwwn
 * @see
 * @since 2026-01-20
 */

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("댓글 작성 성공 - 부모 댓글 없음")
    void createComment_success() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .summary("summary")
                .status(PostStatus.PUBLISHED)
                .build();
        postRepository.save(post);

        // when
        Long commentId = commentService.createComment(
                post.getId(),
                "댓글 내용",
                null
        );

        // then
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        assertThat(comment.getContent()).isEqualTo("댓글 내용");
        assertThat(comment.getPost().getId()).isEqualTo(post.getId());
        assertThat(comment.getParent()).isNull();
    }
}

