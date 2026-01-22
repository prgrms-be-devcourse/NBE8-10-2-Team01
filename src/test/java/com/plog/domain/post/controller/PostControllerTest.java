package com.plog.domain.post.controller;

import com.plog.domain.post.dto.PostCreateReq;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.dto.PostUpdateReq;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest를 사용하여 웹 계층(Controller)만 테스트합니다.
 * JPA, Repository, Service 빈은 로드되지 않으며, MockitoBean을 통해 주입합니다.
 */
@WebMvcTest(PostController.class)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 시 DTO 객체를 JSON으로 변환하여 요청을 검증한다")
    void createPostSuccess() throws Exception {
        // [Given]
        Long mockPostId = 1L;
        PostCreateReq requestDto = new PostCreateReq("테스트 제목", "테스트 본문");

        given(postService.createPost(anyString(), anyString())).willReturn(mockPostId);

        // [When]
        ResultActions resultActions = mvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/posts/%d".formatted(mockPostId)))
                .andExpect(jsonPath("$").doesNotExist());

        verify(postService).createPost(eq(requestDto.title()), eq(requestDto.content()));
    }

    @Test
    @DisplayName("게시글 상세 조회 시 응답 데이터 형식을 확인한다")
    void getPostSuccess() throws Exception {
        // [Given]
        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .build();

        given(postService.getPostDetail(anyLong())).willReturn(PostInfoRes.from(mockPost));

        // [When]
        ResultActions resultActions = mvc
                .perform(get("/api/posts/1"))
                .andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("조회 제목"))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"));
    }

    @Test
    @DisplayName("게시글 목록 조회 시 최신순으로 정렬된 리스트를 반환한다")
    void getPostsSuccess() throws Exception {
        // [Given]
        Post post1 = Post.builder().title("제목1").content("내용1").build();
        Post post2 = Post.builder().title("제목2").content("내용2").build();

        given(postService.getPosts()).willReturn(List.of(
                PostInfoRes.from(post2),
                PostInfoRes.from(post1)
        ));

        // [When]
        ResultActions resultActions = mvc
                .perform(get("/api/posts"))
                .andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("제목2"))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"));
    }

    @Test
    @DisplayName("게시글 수정 요청 시 204 No Content를 반환한다")
    void updatePostSuccess() throws Exception {
        // [Given]
        Long postId = 1L;
        PostUpdateReq requestDto = new PostUpdateReq("수정 제목", "수정 본문");

        // [When]
        ResultActions resultActions = mvc.perform(
                put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        // 객체를 JSON 문자열로 자동 변환
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andDo(print());

        // [Then]
        resultActions.andExpect(status().isNoContent());

        verify(postService).updatePost(
                eq(postId),
                eq(requestDto.title()),
                eq(requestDto.content())
        );
    }

    @Test
    @DisplayName("게시글 삭제 요청 시 성공하면 204 No Content를 반환한다")
    void deletePostSuccess() throws Exception {
        // [When]
        ResultActions resultActions = mvc.perform(
                delete("/api/posts/1")
        ).andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        // 서비스의 deletePost 메서드가 호출되었는지 검증합니다.
        verify(postService).deletePost(1L);
    }
}