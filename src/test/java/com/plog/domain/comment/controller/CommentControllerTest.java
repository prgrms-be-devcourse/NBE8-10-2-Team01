package com.plog.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentUpdateReq;
import com.plog.domain.comment.service.CommentService;
import com.plog.global.exception.errorCode.CommentErrorCode;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.CommentException;
import com.plog.global.exception.exceptions.PostException;
import com.plog.global.security.CustomAuthenticationFilter;
import com.plog.global.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// 이 두 줄이 정확히 있는지 확인하세요
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

// BDDMockito (given을 사용하기 위해)

// Mockito Argument Matchers (any, eq를 사용하기 위해)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
// 만약 willDoNothing을 사용하신다면 아래도 필요합니다.


@WebMvcTest(controllers = CommentController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @Autowired 대신 직접 생성하여 의존성 문제 해결
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean // 최신 버전 방식
    private CommentService commentService;

    @MockitoBean // Security 필터 에러 방지용
    private JwtUtils jwtUtils;

     @Test
     @DisplayName("댓글 생성 성공: 201 Created를 반환한다")
     void createComment_Success() throws Exception {
         // given
         Long postId = 1L;
         CommentCreateReq req = new CommentCreateReq("내용", 1L, null);
         given(commentService.createComment(eq(postId), any(CommentCreateReq.class))).willReturn(100L);

         // when & then
         mockMvc.perform(post("/api/posts/{postId}/comments", postId) // /api 포함
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(req)))
                 .andDo(print())
                 .andExpect(status().isCreated()) // 이제 201이 나올 것입니다.
                 .andExpect(header().string("Location", "/api/posts/1/comments/100"));
     }

     @Test
     @DisplayName("댓글 생성 실패: 내용(content)이 비어있으면 400 Bad Request를 반환한다")
     void createComment_Fail_Validation() throws Exception {
         // given
         Long postId = 1L;
         // @NotBlank 위반 케이스 (빈 문자열)
         CommentCreateReq invalidRequest = new CommentCreateReq("", 1L, null);

         // when & then
         mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(invalidRequest)))
                 .andDo(print())
                 .andExpect(status().isBadRequest());
     }

     @Test
     @DisplayName("대댓글 생성 성공: 부모 댓글 ID가 포함되어도 정상 처리된다")
     void createReply_Success() throws Exception {
         // given
         Long postId = 1L;
         Long parentCommentId = 50L;
         CommentCreateReq replyRequest = new CommentCreateReq("이것은 대댓글입니다.", 1L, parentCommentId);

         given(commentService.createComment(eq(postId), any(CommentCreateReq.class)))
                 .willReturn(101L);

         // when & then
         mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(replyRequest)))
                 .andExpect(status().isCreated());
     }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 성공 시 200 OK를 반환한다.")
    void updateComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        CommentUpdateReq req = new CommentUpdateReq("수정된 댓글 내용입니다.");

        // when & then
        mockMvc.perform(put("/api/comments/{commentId}", commentId) // PUT 요청 수행
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))) // Body에 JSON 데이터 삽입
                .andExpect(status().isOk()); // 리턴값이 200 OK인지 검증
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 요청 시 내용(content)이 없으면 400 Bad Request를 반환한다.")
    void updateComment_Fail_Validation() throws Exception {
        // given
        Long commentId = 1L;
        CommentUpdateReq req = new CommentUpdateReq(""); // 빈 내용 (Validation 위반 가정)

        // when & then
        mockMvc.perform(put("/api/comments/1") //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest()); // @Valid에 의해 400 에러 발생
    }


    @Test
    @DisplayName("댓글 삭제 성공: 200 OK와 삭제된 ID를 반환한다")
    void deleteComment_Success() throws Exception {
        // given
        Long commentId = 100L;

        // deleteComment는 void 메서드이므로 별도의 willReturn은 필요 없지만,
        // 명시적으로 아무 일도 일어나지 않음을 선언할 수 있습니다.
        willDoNothing().given(commentService).deleteComment(commentId);

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(commentId))
                .andExpect(jsonPath("$.message").value("댓글 삭제 완료"));

        // 서비스 메서드가 실제로 호출되었는지 검증
        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("댓글 수정 실패: 존재하지 않는 댓글을 수정하려 하면 404 Not Found를 반환한다")
    void updateComment_Fail_NotFound() throws Exception {
        // given
        Long commentId = 999L;
        CommentUpdateReq req = new CommentUpdateReq("수정 내용");

        // 서비스에서 CommentException을 던지도록 스텁 설정
        willThrow(new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, "Not Found", "존재하지 않는 댓글입니다."))
                .given(commentService).updateComment(eq(commentId), anyString());

        // when & then
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isNotFound()) // 404 검증
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    @DisplayName("대댓글 조회 실패: 부모 댓글이 존재하지 않으면 404 Not Found를 반환한다")
    void getReplies_Fail_ParentNotFound() throws Exception {
        // given
        Long commentId = 999L;

        // any(Pageable.class) -> anyInt() 또는 eq(0)으로 변경
        given(commentService.getRepliesByCommentId(eq(commentId), anyInt()))
                .willThrow(new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, "Parent Not Found", "존재하지 않는 댓글입니다."));

        // when & then
        mockMvc.perform(get("/api/comments/{commentId}/replies", commentId)
                        .param("offset", "0")) // 파라미터명도 'page'에서 'offset'으로 확인!
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 생성 실패: 게시글이 존재하지 않으면 404 Not Found를 반환한다")
    void createComment_Fail_PostNotFound() throws Exception {
        // given
        Long postId = 999L;
        CommentCreateReq req = new CommentCreateReq("내용", 1L, null);

        // 서비스에서 PostException을 던지는 상황 가정
        given(commentService.createComment(eq(postId), any(CommentCreateReq.class)))
                .willThrow(new PostException(PostErrorCode.POST_NOT_FOUND, "Post Not Found", "존재하지 않는 게시글입니다."));

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }


}