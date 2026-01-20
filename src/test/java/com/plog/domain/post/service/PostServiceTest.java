package com.plog.domain.post.service;

import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
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

        given(postRepository.save(ArgumentMatchers.any(Post.class)))
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

        given(postRepository.save(ArgumentMatchers.any(Post.class)))
                .willReturn(mockPost);

        postService.createPost(title, longContent);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertThat(savedPost.getSummary().length()).isEqualTo(153);
        assertThat(savedPost.getSummary()).endsWith("...");
    }
}
