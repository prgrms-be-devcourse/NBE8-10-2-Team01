
package com.plog.domain.post.controller;

import com.plog.domain.post.entity.Post;
import com.plog.domain.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 시 JSON 문자열을 직접 전달하여 검증한다")
    void createPost_Success() throws Exception {
        // [Given]
        Long mockPostId = 1L;
        given(postService.createPost(anyString(), anyString())).willReturn(mockPostId);

        // [When]
        ResultActions resultActions = mvc
                .perform(
                        post("/api/posts") // 현재 컨트롤러 경로에 맞춤
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 본문"
                                        }
                                        """)
                )
                .andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(mockPostId))
                .andExpect(jsonPath("$.message").value("게시글 작성 성공"));
    }

    @Test
    @DisplayName("게시글 상세 조회 시 응답 데이터 형식을 확인한다")
    void getPost_Success() throws Exception {
        // [Given]
        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .build();
        given(postService.getPostDetail(anyLong())).willReturn(mockPost);

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
}