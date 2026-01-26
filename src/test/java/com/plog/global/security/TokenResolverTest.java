package com.plog.global.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TokenResolver}의 토큰 추출 및 설정 로직을 검증하는 단위 테스트입니다.
 *
 * @author minhee
 * @since 2026-01-23
 */
class TokenResolverTest {

    private TokenResolver tokenResolver;

    /**
     * 테스트용 설정값입니다.
     */
    private final long EXPIRATION = 3600000;
    private final String DOMAIN = "localhost";
    private final boolean SECURE = false;

    @BeforeEach
    void setUp() {
        tokenResolver = new TokenResolver(EXPIRATION, DOMAIN, SECURE);
    }

    @Test
    @DisplayName("Access Token 추출 - Authorization 헤더에 Bearer 토큰이 있으면 접두사를 제거하고 반환한다")
    void resolveAccessToken_success() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mock-token-value");

        // when
        String result = tokenResolver.resolveAccessToken(request);

        // then
        assertThat(result).isEqualTo("mock-token-value");
    }

    @Test
    @DisplayName("Refresh Token 추출 - 쿠키에 refreshToken이 존재하면 값을 반환한다")
    void resolveRefreshToken_success() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("refreshToken", "refresh-token-value");
        request.setCookies(cookie);

        // when
        String result = tokenResolver.resolveRefreshToken(request);

        // then
        assertThat(result).isEqualTo("refresh-token-value");
    }

    @Test
    @DisplayName("토큰 응답 설정 - 헤더에는 Bearer AT가, 쿠키에는 HttpOnly RT가 설정되어야 한다")
    void setTokenResponse_test() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String access = "new-access";
        String refresh = "new-refresh";

        // when
        tokenResolver.setHeader(response, access);
        tokenResolver.setCookie(response, refresh);

        // then
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer new-access");

        Cookie cookie = response.getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("new-refresh");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("추출 실패 케이스 - 토큰이 없으면 null을 반환한다")
    void resolve_fail_returnsNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when & then
        assertThat(tokenResolver.resolveAccessToken(request)).isNull();
        assertThat(tokenResolver.resolveRefreshToken(request)).isNull();
    }
}