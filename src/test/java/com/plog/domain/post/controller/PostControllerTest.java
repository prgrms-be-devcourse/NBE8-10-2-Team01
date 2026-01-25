package com.plog.domain.post.controller;

import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.service.PostService;
import com.plog.global.security.CustomAuthenticationFilter;
import com.plog.global.security.CustomUserDetailsService;
import com.plog.global.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest를 사용하여 웹 계층(Controller)만 테스트합니다.
 * JPA, Repository, Service 빈은 로드되지 않으며, MockitoBean을 통해 주입합니다.
 */

@ExtendWith(MockitoExtension.class)
class PostControllerTest {


    private MockMvc mvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomAuthenticationFilter customAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(postController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("게시글 상세 조회 시 대댓글 정보가 포함되었는지 확인한다")
    void getPostWithRepliesSuccess() throws Exception {
        // [Given]
        Slice<ReplyInfoRes> replySlice = new SliceImpl<>(new ArrayList<>(), PageRequest.of(0, 5), false);


        CommentInfoRes parentComment = new CommentInfoRes(
                1L,
                "부모 댓글 내용입니다.",
                100L,
                "작성자",
                0L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L,
                replySlice
        );

        Slice<CommentInfoRes> commentSlice = new SliceImpl<>(
                List.of(parentComment),
                PageRequest.of(0, 10),
                false
        );

        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .build();
        ReflectionTestUtils.setField(mockPost, "id", 1L);

        PostInfoRes mockRes = PostInfoRes.from(mockPost, commentSlice);
        given(postService.getPostDetail(anyLong(), anyInt())).willReturn(mockRes);

        // [When & Then]
        mvc.perform(get("/api/posts/1")
                        .param("comment_offset", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.content[0].content").value("부모 댓글 내용입니다."))
                .andExpect(jsonPath("$.data.comments.content[0].previewReplies.content").isArray())
                .andExpect(jsonPath("$.data.comments.content[0].previewReplies.empty").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 생성 시 JSON 문자열을 직접 전달하여 검증한다")
    void createPostSuccess() throws Exception {
        // [Given]
        // 1. Post 객체 생성 (id 제외)
        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .summary("요약글")
                .build();

        // 2. 부모로부터 상속받은 id 필드에 강제로 값 주입 (핵심!)
        ReflectionTestUtils.setField(mockPost, "id", 1L);

        // 3. 슬라이싱 페이징 데이터 준비 (지난번에 요청하신 슬라이싱 페이징 구조!)
        List<CommentInfoRes> commentList = new ArrayList<>();
        Slice<CommentInfoRes> mockSlice = new SliceImpl<>(commentList, PageRequest.of(0, 10), false);

        PostInfoRes mockRes = PostInfoRes.from(mockPost, mockSlice);

        given(postService.getPostDetail(anyLong(), anyInt())).willReturn(mockRes);

        // [When & Then]
        mvc.perform(get("/api/posts/1")
                        .param("comment_offset", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("조회 제목"))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"));
    }


    @Test
    @DisplayName("게시글 상세 조회 시 슬라이싱 페이징이 포함된 응답을 확인한다")
    void getPostSuccess() throws Exception {
        // [Given]
        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .build();
        ReflectionTestUtils.setField(mockPost, "id", 1L);

        List<CommentInfoRes> commentList = new ArrayList<>();

        Slice<CommentInfoRes> emptyComments = new SliceImpl<>(commentList, PageRequest.of(0, 10), false);

        PostInfoRes mockRes = PostInfoRes.from(mockPost, emptyComments);

        given(postService.getPostDetail(anyLong(), anyInt())).willReturn(mockRes);

        // [When & Then]
        mvc.perform(get("/api/posts/1")
                        .param("comment_offset", "0"))
                .andDo(print())
                .andExpect(status().isOk()) // 이제 500이 아닌 200이 뜰 겁니다!
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
}