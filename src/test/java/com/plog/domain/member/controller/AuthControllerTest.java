package com.plog.domain.member.controller;


import com.plog.domain.member.entity.Member;
import com.plog.domain.member.service.AuthService;
import com.plog.global.rq.Rq;
import com.plog.global.security.JwtUtils;
import com.plog.testUtil.WebMvcTestSupport;
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
 *  {@link AuthController} 에 대한 슬라이스 테스트 입니다.
 *
 * @author minhee
 * @since 2026-01-21
 */

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends WebMvcTestSupport {

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private Rq rq;
    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("회원가입 성공 - 201, Location 헤더를 반환")
    void signUp_success() throws Exception {
        // given
        Long memberId = 1L;
        AuthController.MemberSignUpReq req = new AuthController.MemberSignUpReq(
                "test@plog.com",
                "password123!",
                "plogger"
        );

        given(authService.signUp(anyString(), anyString(), anyString())).willReturn(memberId);

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
        AuthController.MemberSignUpReq req = new AuthController.MemberSignUpReq(
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
    @DisplayName("로그인 성공 - 200, AccessToken을 반환")
    void signIn_success() throws Exception {
        // given
        String email = "test@plog.com";
        String password = "password123!";
        String nickname = "plogger";
        String accessToken = "mock-access-token";
        String refreshToken = "mock-refresh-token";

        Member mockMember = Member.builder()
                .email(email)
                .nickname(nickname)
                .build();

        AuthController.MemberSignInReq req = new AuthController.MemberSignInReq(email, password);

        given(authService.signIn(anyString(), anyString())).willReturn(mockMember);
        given(authService.genAccessToken(any(Member.class))).willReturn(accessToken);
        given(authService.genRefreshToken(any(Member.class))).willReturn(refreshToken);

        // when
        ResultActions result = mockMvc.perform(post("/api/members/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken));

        // Rq를 통해 헤더와 쿠키가 설정되었는지 검증
        verify(rq).setHeader("Authorization", accessToken);
        verify(rq).setCookie("apiKey", refreshToken);
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 공백, 400 에러 반환")
    void signIn_fail_emptyEmail() throws Exception {
        // given
        AuthController.MemberSignInReq req = new AuthController.MemberSignInReq("", "password123!");

        // when & then
        mockMvc.perform(post("/api/members/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 성공 - 200, 쿠키 삭제 확인")
    void logout_success() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/members/logout")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

        // Rq를 통해 쿠키가 삭제되었는지 검증
        verify(rq).deleteCookie("apiKey");
    }

    @Test
    @DisplayName("토큰 재발급 성공 - 200, 새 AccessToken 반환")
    void accessTokenReissue_success() throws Exception {
        // given
        String refreshToken = "mock-refresh-token";
        String newAccessToken = "new-access-token";

        given(rq.getCookieValue(eq("apiKey"), any())).willReturn(refreshToken);
        given(authService.accessTokenReissue(refreshToken)).willReturn(newAccessToken);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/reissue")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.nickname").value("reissued"));

        // 헤더에 새 토큰이 설정되었는지 검증
        verify(rq).setHeader("Authorization", newAccessToken);
    }
}