package com.plog.global.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.plog.domain.member.dto.AuthInfoRes;
import com.plog.domain.member.dto.AuthSignInReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.response.CommonResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 사용자 로그인을 처리하고 JWT 토큰을 발급하는 필터입니다.
 * <p>
 * JSON 바디를 통한 로그인을 처리하며,
 * 인증 성공 시 Access Token(헤더)과 Refresh Token(HttpOnly 쿠키)을 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link UsernamePasswordAuthenticationFilter}를 상속받아 인증 시도 및 성공/실패 로직을 재정의합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code LoginFilter(AuthenticationManager, ObjectMapper, JwtUtils, ...)} <br>
 *  * 인증 처리를 위한 매니저와 토큰 생성 유틸리티, 쿠키 설정을 위한 외부 환경 변수를 주입받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@link SecurityConfig}에서 {@code @Bean} 대신 직접 인스턴스화하여 필터 체인에 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Jackson {@link ObjectMapper}를 통해 JSON 요청을 파싱합니다.
 *
 * @author minhee
 * @see com.plog.global.security.SecurityConfig
 * @since 2026-01-22
 */

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JwtUtils jwtUtils;
    private final TokenResolver tokenResolver;

    public LoginFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper, JwtUtils jwtUtils, TokenResolver tokenResolver) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.jwtUtils = jwtUtils;
        this.tokenResolver = tokenResolver;

        setFilterProcessesUrl("/api/members/sign-in");
    }

    /**
     * HTTP 요청 바디에서 이메일과 비밀번호를 추출하여 인증을 시도합니다.
     *
     * @param request  HTTP 요청 (JSON 바디 포함)
     * @param response HTTP 응답
     * @return 인증 객체 {@link Authentication}
     * @throws AuthenticationException 인증 실패 시 발생
     */
    @NotNull
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            AuthSignInReq authSignInReq = objectMapper.readValue(body, AuthSignInReq.class);
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    authSignInReq.email(),
                    authSignInReq.password()
            );
            return authenticationManager.authenticate(authRequest);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 인증이 성공했을 때 실행되며, JWT 토큰을 생성하여 클라이언트에게 반환합니다.
     * <p>
     * - Access Token: Authorization 헤더에 Bearer 타입으로 전달 <br>
     * - Refresh Token: 보안을 위해 HttpOnly 쿠키에 저장
     */
    @Override
    protected void successfulAuthentication(
            @NotNull HttpServletRequest request, HttpServletResponse response, @NotNull FilterChain chain, Authentication authentication) throws IOException, ServletException {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        MemberInfoRes memberInfo = MemberInfoRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();

        String accessToken = jwtUtils.createAccessToken(memberInfo);
        String refreshToken = jwtUtils.createRefreshToken(user.getEmail());
        tokenResolver.setHeader(response, accessToken);
        tokenResolver.setCookie(response, refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        AuthInfoRes authInfoRes = AuthInfoRes.builder()
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .build();

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.success(authInfoRes,
                                authInfoRes.nickname() + "님 환영합니다.")
                )
        );
    }

    /**
     * 인증에 실패했을 때 실행되며, 공통 에러 응답 형식을 반환합니다.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("[LoginFilter#unsuccessfulAuthentication] Login failed for user: {}", failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.fail(AuthErrorCode.INVALID_CREDENTIALS.getMessage())
                )
        );
    }
}