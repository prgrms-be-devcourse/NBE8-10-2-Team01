package com.plog.domain.post.service;

import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.exceptions.PostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PostServiceTest {
    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 저장 시 마크다운이 제거된 요약글이 자동 생성")
    void createPostSuccess() {
        String title = "테스트 제목";
        String content = "# Hello\n**Spring Boot**";

        Post mockPost = Post.builder()
                .title(title)
                .content(content)
                .build();

        given(postRepository.save(any(Post.class)))
                .willReturn(mockPost);

        postService.createPost(title, content);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getSummary()).isEqualTo("Hello\nSpring Boot");
    }

    @Test
    @DisplayName("본문이 150자를 초과하면 요약글은 150자까지만 저장되고 말줄임표가 붙는다")
    void createPostSuccessSummaryTruncation() {
        String title = "제목";
        String longContent = "가".repeat(200);

        Post mockPost = Post.builder()
                .title(title)
                .content(longContent)
                .build();

        given(postRepository.save(any(Post.class)))
                .willReturn(mockPost);

        postService.createPost(title, longContent);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertThat(savedPost.getSummary().length()).isEqualTo(153);
        assertThat(savedPost.getSummary()).endsWith("...");
    }
    @Test
    @DisplayName("게시글 수정 시 본문에 맞춰 요약본이 새롭게 생성되어야 한다")
    void updatePostSuccess() {
        // [Given]
        Long postId = 1L;
        Post existingPost = Post.builder()
                .title("기존 제목")
                .content("기존 본문")
                .summary("기존 요약")
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        String newTitle = "수정된 제목";
        String newContent = "수정된 본문 내용입니다. 이 내용은 150자 미만이므로 그대로 요약이 됩니다.";

        // [When]
        postService.updatePost(postId, newTitle, newContent);

        // [Then]
        // 더티 체킹에 의해 변경될 엔티티의 상태를 검증합니다.
        assertThat(existingPost.getTitle()).isEqualTo(newTitle);
        assertThat(existingPost.getContent()).isEqualTo(newContent);
        assertThat(existingPost.getSummary()).contains("수정된 본문"); // 요약본 갱신 확인
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 PostException이 발생한다")
    void updatePostFailNotFound() {
        // [Given]
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // [When & Then]
        assertThatThrownBy(() -> postService.updatePost(99L, "제목", "내용"))
                .isInstanceOf(PostException.class)
                .hasMessageContaining("존재하지 않는 게시물입니다.");
    }

    @Test
    @DisplayName("게시글 삭제 시 해당 ID의 게시글이 존재하면 삭제를 수행한다")
    void deletePostSuccess() {
        // [Given]
        Long postId = 1L;
        Post post = Post.builder()
                .title("삭제될 제목")
                .content("삭제될 본문")
                .build();

        // findById 호출 시 삭제할 게시글이 있다고 가정합니다.
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // [When]
        postService.deletePost(postId);

        // [Then]
        // 1. findById가 호출되었는지 확인
        verify(postRepository).findById(postId);
        // 2. 실제 리포지토리의 delete 메서드가 해당 엔티티로 호출되었는지 확인
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 삭제 요청 시 PostException이 발생한다")
    void deletePostFailNotFound() {
        // [Given]
        Long postId = 99L;
        // findById 호출 시 빈 값을 반환한다고 가정합니다.
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // [When & Then]
        // 예외가 발생하는지 확인합니다.
        assertThatThrownBy(() -> postService.deletePost(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining("존재하지 않는 게시물입니다.");

        // 예외가 발생했으므로 실제 delete 메서드는 호출되지 않아야 합니다.
        verify(postRepository, never()).delete(any(Post.class));
    }
}
