package com.plog.domain.post.controller;

import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.member.entity.Member;
import com.plog.domain.post.dto.PostCreateReq;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.dto.PostListRes;
import com.plog.domain.post.dto.PostUpdateReq;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.service.PostService;
import com.plog.global.security.*;
import com.plog.testUtil.SecurityTestConfig;
import com.plog.testUtil.WebMvcTestSupport;
import com.plog.testUtil.WithCustomMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest를 사용하여 웹 계층(Controller)만 테스트합니다.
 * JPA, Repository, Service 빈은 로드되지 않으며, MockitoBean을 통해 주입합니다.
 */
@WebMvcTest(PostController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
class PostControllerTest extends WebMvcTestSupport {

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 시 인증된 사용자가 요청하면 성공한다")
    @WithCustomMockUser
    void createPostSuccess() throws Exception {
        // [Given]
        Long mockMemberId = 1L;
        Long createdPostId = 100L;

        PostCreateReq request = new PostCreateReq("게시글 제목", "게시글 본문", null, "example.com");

        given(postService.createPost(eq(mockMemberId), any(PostCreateReq.class)))
                .willReturn(createdPostId);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/posts/" + createdPostId));
    }

    @Test
    @DisplayName("게시글 상세 조회 시 응답 데이터 형식을 확인한다")
    void getPostSuccess() throws Exception {
        // [Given]
        Member author = new Member("email", "password", "nickname", null);
        Post mockPost = Post.builder()
                .title("조회 제목")
                .content("조회 본문")
                .member(author)
                .build();

        Slice<CommentInfoRes> mockComments = new SliceImpl<>(Collections.emptyList());

        given(postService.getPostDetail(anyLong(), anyInt())).willReturn(PostInfoRes.from(mockPost));

        // [When]
        ResultActions resultActions = mockMvc
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
    @DisplayName("게시글 목록 조회 시 페이징 처리된 Slice 리스트를 반환한다")
    void getPostsSuccess() throws Exception {
        // [Given]
        Member author = new Member("email", "password", "nickname", null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Post post1 = Post.builder().title("제목1").content("내용1").member(author).build();
        Post post2 = Post.builder().title("제목2").content("내용2").member(author).build();

        Slice<PostListRes> sliceResponse = new SliceImpl<>(
                List.of(PostListRes.from(post2), PostListRes.from(post1)),
                pageable,
                false // 다음 페이지가 없다고 가정
        );

        given(postService.getPosts(any(Pageable.class))).willReturn(sliceResponse);

        // [When]
        ResultActions resultActions = mockMvc
                .perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("제목2"))
                .andExpect(jsonPath("$.data.content[1].title").value("제목1"))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"));
    }

    @Test
    @DisplayName("게시글 수정 요청 시 204 No Content를 반환한다")
    @WithCustomMockUser
    void updatePostSuccess() throws Exception {
        // [Given]
        Long postId = 1L;
        PostUpdateReq requestDto = new PostUpdateReq("수정 제목", "수정 본문", null, null);

        // [When]
        ResultActions resultActions = mockMvc.perform(
                put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andDo(print());

        // [Then]
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("게시글 삭제 요청 시 성공하면 204 No Content를 반환한다")
    @WithCustomMockUser
    void deletePostSuccess() throws Exception {
        // [When]
        ResultActions resultActions = mockMvc.perform(
                delete("/api/posts/1")
        ).andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        // 서비스의 deletePost 메서드가 호출되었는지 검증합니다.
        verify(postService).deletePost(any(), any());
    }

    @Test
    @DisplayName("특정 회원의 게시글 목록 조회 시 Slice 구조와 상세 DTO 필드들이 JSON에 포함되어야 한다")
    void getPostsByMemberApiSuccess() throws Exception {
        // [Given]
        Long memberId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10); // 테스트용 페이징 정보

        // PostInfoRes 데이터 준비
        PostInfoRes res = new PostInfoRes(
                100L, "제목", "본문", 5, now, now, null, null, null, 1L, "nickname", "imageURL"
        );

        // SliceImpl을 사용하여 서비스 반환값 모킹 (데이터 1개, 다음 페이지 없음)
        Slice<PostInfoRes> sliceResponse = new SliceImpl<>(List.of(res), pageable, false);

        // 서비스 메서드 호출 시 Pageable 파라미터를 포함하도록 설정
        given(postService.getPostsByMember(eq(memberId), any(Pageable.class)))
                .willReturn(sliceResponse);

        // [When]
        ResultActions resultActions = mockMvc.perform(
                get("/api/posts/members/{memberId}", memberId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // [Then]
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                // Slice 구조에서는 데이터가 'content' 필드 안에 배열로 들어갑니다.
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(100))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"))
                .andExpect(jsonPath("$.data.content[0].content").value("본문"))
                .andExpect(jsonPath("$.data.content[0].viewCount").value(5))
                .andExpect(jsonPath("$.data.content[0].createDate").exists())
                .andExpect(jsonPath("$.data.content[0].modifyDate").exists())
                // Slice 메타데이터 검증 (다음 페이지 여부 등)
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.message").value("사용자 게시글 목록 조회 성공"));

        // 서비스 계층으로 정확한 인자가 전달되었는지 확인합니다.
        verify(postService).getPostsByMember(eq(memberId), any(Pageable.class));
    }
}
