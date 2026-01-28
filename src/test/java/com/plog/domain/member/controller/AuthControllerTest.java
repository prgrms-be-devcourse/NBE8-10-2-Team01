package com.plog.domain.member.controller;


import com.plog.domain.member.dto.AuthSignInReq;
import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.service.AuthService;
import com.plog.testUtil.WebMvcTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link AuthController} 에 대한 슬라이스 테스트 입니다.
 *
 * @author minhee
 * @since 2026-01-21
 */

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends WebMvcTestSupport {

    @Test
    @DisplayName("회원가입 성공 - 201, Location 헤더를 반환")
    void signUp_success() throws Exception {
        // given
        Long memberId = 1L;
        AuthSignUpReq req = new AuthSignUpReq(
                "test@plog.com",
                "password123!",
                "plogger"
        );

        given(authService.signUp(any(AuthSignUpReq.class))).willReturn(memberId);

        // when
        ResultActions result = mockMvc.perform(post("/api/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/members/" + memberId));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 올바르지 않음, 400 에러 반환.")
    void signUp_fail_invalidEmail() throws Exception {
        // given
        AuthSignUpReq req = new AuthSignUpReq(
                "invalid-email",
                "password123!",
                "plogg"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 공백, 400 에러 반환")
    void signIn_fail_emptyEmail() throws Exception {
        // given
        AuthSignInReq req = new AuthSignInReq("", "password123!");

        // when & then
        mockMvc.perform(post("/api/members/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 성공 - 200, 쿠키 삭제 확인")
    void logout_success() throws Exception {
        // given
        String refreshToken = "refresh-token";
        given(tokenResolver.resolveRefreshToken(any())).willReturn(refreshToken);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/logout")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

        // 쿠키가 삭제되었는지 검증
        verify(authService).logout(eq(refreshToken));
        verify(tokenResolver).deleteRefreshTokenCookie(any(HttpServletResponse.class));
    }
}