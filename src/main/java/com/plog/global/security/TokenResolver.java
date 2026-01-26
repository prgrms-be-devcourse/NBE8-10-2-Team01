package com.plog.global.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * HTTP 요청과 응답 메시지 사이에서 JWT 토큰의 운반을 전담하는 컴포넌트입니다.
 * <p>
 * 클라이언트의 헤더(Authorization)나 쿠키(refreshToken)로부터 토큰을 추출하고
 * 생성되거나 재발급된 토큰을 헤더 및 쿠키에 설정합니다.
 *
 * <p><b>작동 원리:</b><br>
 *  - 추출: HttpServletRequest에서 Bearer 타입의 헤더와 지정된 이름의 쿠키를 탐색합니다.<br>
 *  - 설정: HttpOnly, Secure, SameSite(Lax) 등의 속성이 적용된 쿠키를 생성합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code TokenResolver(long refreshExpiration, String cookieDomain, boolean cookieSecure)}<br>
 * 외부 설정 파일(application.yml)로부터 쿠키의 만료 시간, 도메인, 보안 연결 여부를 주입받아 초기화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link Component} 어노테이션을 통해 스프링 빈으로 등록되며,
 * 인증 필터(LoginFilter, CustomAuthenticationFilter)에서 의존성 주입을 통해 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * akarta Servlet API 및 Spring Framework의 HTTP 유틸리티를 활용합니다.
 *
 * @author minhee
 * @since 2026-01-23
 */

@Component
public class TokenResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private final long refreshExpiration;
    private final String cookieDomain;
    private final boolean cookieSecure;

    public TokenResolver(
            @Value("${custom.jwt.refresh-expiration}") long refreshExpiration,
            @Value("${custom.cookie.domain}") String cookieDomain,
            @Value("${custom.cookie.secure}") boolean cookieSecure) {
        this.refreshExpiration = refreshExpiration;
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
    }

    /**
     * HTTP 요청 헤더에서 Access Token(Bearer)을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return "Bearer " 접두사가 제거된 순수 토큰 문자열, 추출 실패 시 {@code null}
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * HTTP 요청 쿠키에서 Refresh Token을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 쿠키에 저장된 Refresh Token 문자열, 존재하지 않을 경우 {@code null}
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * HTTP 응답 헤더에 Access Token을 설정합니다.
     *
     * @param response HTTP 응답 객체
     * @param access 전달할 Access Token 문자열
     */
    public void setHeader(HttpServletResponse response, String access) {
        response.setHeader("Authorization", "Bearer " + access);
    }

    /**
     * HTTP 응답 쿠키에 Refresh Token을 보안 설정과 함께 추가합니다.
     * <p>
     * 적용된 보안 설정: HttpOnly, SameSite.Lax, Secure
     *
     * @param response HTTP 응답 객체
     * @param refresh 전달할 Refresh Token 문자열
     */
    public void setCookie(HttpServletResponse response, String refresh) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                .path("/")
                .domain(cookieDomain.equals("localhost") ? null : cookieDomain)
                .secure(cookieSecure)
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(refreshExpiration / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // TokenResolver.java

    /**
     * Refresh Token 쿠키를 삭제(무효화)합니다.
     * <p>
     * 만료 시간을 0으로 설정한 쿠키를 응답에 추가하여 브라우저가 즉시 삭제하도록 유도합니다.
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .path("/")
                .domain(cookieDomain.equals("localhost") ? null : cookieDomain)
                .maxAge(0) // 즉시 만료
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}