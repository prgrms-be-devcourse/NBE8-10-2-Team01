package com.plog.global.security;


import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link CustomAuthenticationFilter}의 인증 처리 및 자동 토큰 재발급을 검증하는 테스트입니다.
 * <p>
 * 유효한 토큰을 통한 인증 성공 케이스와 Access Token 만료 시 Refresh Token을 활용한
 * 재발급 과정이 설계된 대로 동작하는지 확인합니다.
 *
 * @author minhee
 * @since 2026-01-23
 */

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFilterTest {

    @InjectMocks
    private CustomAuthenticationFilter filter;
    @Mock
    private JwtUtils jwtUtils;
    @Mock private TokenResolver tokenResolver;
    @Mock private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("자동 재발급 - Access Token 만료 시 Refresh Token이 유효하면 새 토큰을 헤더에 설정한다")
    void handleAccessTokenReissue_success() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        String expiredAt = "expired_access_token";
        String validRt = "valid_refresh_token";
        String newAt = "new_access_token";
        String email = "test@plog.com";

        // Access Token 추출 및 만료 예외 발생 시뮬레이션
        given(tokenResolver.resolveAccessToken(request)).willReturn(expiredAt);
        given(jwtUtils.parseToken(expiredAt)).willThrow(ExpiredJwtException.class);

        // Refresh Token 처리 로직 모킹
        given(tokenResolver.resolveRefreshToken(request)).willReturn(validRt);
        Claims rtClaims = mock(Claims.class);
        given(rtClaims.getSubject()).willReturn(email);
        given(jwtUtils.parseToken(validRt)).willReturn(rtClaims);

        // 서비스 및 유틸리티 모킹
        SecurityUser user = SecurityUser.securityUserBuilder()
                .id(1L).email(email).nickname("plogger").authorities(List.of()).build();
        given(customUserDetailsService.loadUserByUsername(email)).willReturn(user);
        given(jwtUtils.createAccessToken(any(MemberInfoRes.class))).willReturn(newAt);

        Claims newAtClaims = mock(Claims.class);
        given(newAtClaims.getSubject()).willReturn(email);
        given(newAtClaims.get("id", Long.class)).willReturn(1L);
        given(newAtClaims.get("nickname", String.class)).willReturn("plogger");
        given(jwtUtils.parseToken(newAt)).willReturn(newAtClaims);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(tokenResolver).setHeader(response, "new_access_token");
        verify(filterChain).doFilter(request, response);
    }
}