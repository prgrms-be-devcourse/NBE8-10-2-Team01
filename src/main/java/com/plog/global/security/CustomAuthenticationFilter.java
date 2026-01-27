package com.plog.global.security;


import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.global.exception.errorCode.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검사하는 인증 필터입니다.
 * <p>
 * 요청 헤더에서 Access Token을 추출하여 검증합니다. 만약 Access Token이 만료되었다면
 * 쿠키의 Refresh Token과 {@link TokenStore}(Caffeine)를 대조하여 Access Token을 자동으로 재발급합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link OncePerRequestFilter}를 상속받아 하나의 요청당 단 한 번만 실행됨을 보장합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code CustomAuthenticationFilter(JwtUtils, TokenResolver, CustomUserDetailsService, TokenStore)}<br>
 * 인증 유지 및 토큰 재발급에 필요한 보안 컴포넌트들을 주입받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link Component} 어노테이션을 통해 스프링 빈으로 등록되며,
 * SecurityConfig에서 필터 체인의 적절한 위치에 수동으로 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Security Core 및 Servlet API를 사용합니다.
 *
 * @author minhee
 * @see SecurityContextHolder
 * @see JwtUtils
 * @since 2026-01-16
 */

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final TokenResolver tokenResolver;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenStore tokenStore;

    /**
     * 필터의 핵심 로직을 수행하며, Access Token의 유효성을 검사하고 만료 시 재발급을 시도합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = tokenResolver.resolveAccessToken(request);

        try {
            if (accessToken != null) {
                authenticate(accessToken);
            }
        } catch (ExpiredJwtException e) {
            handleAccessTokenReissue(request, response);
        } catch (Exception e) {
            request.setAttribute("exception", AuthErrorCode.TOKEN_INVALID);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 검증된 {@link SecurityUser} 정보를 바탕으로 Spring Security 인증 객체를 생성하여 컨텍스트에 등록합니다.
     * @param user 인증된 사용자 정보 객체
     */
    private void processAuthentication(SecurityUser user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * 전달받은 Access Token을 파싱하여 Spring Security 인증 객체를 생성하고 컨텍스트에 등록합니다.
     * <p>
     * 토큰의 Claims에서 사용자의 PK(id), 식별자(email), 닉네임을 추출하여 {@link SecurityUser}를 구성합니다.
     *
     * @param token 파싱할 JWT Access Token 문자열
     */
    private void authenticate(String token) {
        Claims claims = jwtUtils.parseToken(token);
        Long id = claims.get("id", Long.class);
        String email = claims.getSubject();
        String nickname = claims.get("nickname", String.class);

        SecurityUser user = SecurityUser.securityUserBuilder()
                .id(id)
                .email(email)
                .password("")
                .nickname(nickname != null ? nickname : "")
                .authorities(List.of())
                .build();

        processAuthentication(user);
    }

    /**
     * Access Token 만료 시 자동 재발급을 수행합니다.
     * <p>
     * 쿠키의 Refresh Token과 {@link TokenStore}에 저장된 토큰이 일치하는지 확인합니다.
     * 일치할 경우 새 Access Token을 생성하여 응답 헤더에 담고 인증을 승인합니다.
     * 검증 실패(만료, 불일치 등) 시 관련 쿠키를 제거하고 로그인 유도 예외를 설정합니다.
     *
     * @param request  RefreshToken 추출용
     * @param response 새 토큰 전달 및 쿠키 삭제용
     */
    private void handleAccessTokenReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = tokenResolver.resolveRefreshToken(request);

        if (refreshToken == null) {
            request.setAttribute("exception", AuthErrorCode.LOGIN_REQUIRED);
            return;
        }

        try {
            Claims claims = jwtUtils.parseToken(refreshToken);
            String email = claims.getSubject();
            String savedToken = tokenStore.get(email);

            if (savedToken == null || !savedToken.equals(refreshToken)) {
                tokenResolver.deleteRefreshTokenCookie(response);
                request.setAttribute("exception", AuthErrorCode.LOGIN_REQUIRED);
                return;
            }

            SecurityUser user = (SecurityUser) customUserDetailsService.loadUserByUsername(email);
            MemberInfoRes memberInfo = MemberInfoRes.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .build();

            String newAccess = jwtUtils.createAccessToken(memberInfo);
            tokenResolver.setHeader(response, newAccess);

            processAuthentication(user);
        } catch (ExpiredJwtException e) {
            tokenResolver.deleteRefreshTokenCookie(response);
            request.setAttribute("exception", AuthErrorCode.LOGIN_REQUIRED);
        } catch (Exception e) {
            request.setAttribute("exception", AuthErrorCode.TOKEN_INVALID);
        }
    }
}