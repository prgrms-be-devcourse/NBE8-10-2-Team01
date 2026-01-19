package com.plog.global.security;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검사하는 인증 필터입니다.
 * <p>
 * 요청 헤더(Authorization) 또는 쿠키(accessToken)에서 토큰을 추출하여 검증합니다.
 * 유효한 토큰일 경우 사용자의 인증 정보({@link SecurityUser})를 생성하고,
 * 이를 {@link SecurityContextHolder}에 저장하여 해당 요청 세션 동안 인증 상태를 유지합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link OncePerRequestFilter}를 상속받아 하나의 요청당 단 한 번만 실행됨을 보장합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code CustomAuthenticationFilter(JwtUtils jwtUtils)}<br>
 * 토큰 파싱 및 검증 로직을 담당하는 JwtUtils를 주입받아 초기화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link Component} 어노테이션을 통해 스프링 빈으로 등록되며,
 * SecurityConfig에서 필터 체인의 적절한 위치에 수동으로 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Security Core 및 Servlet API를 사용합니다.
 *
 * @author minhee
 * @since 2026-01-16
 * @see SecurityContextHolder
 * @see JwtUtils
 */

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    /**
     * 필터의 핵심 로직을 수행하며, 토큰 유무에 따라 인증 정보를 컨텍스트에 저장합니다.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: API 요청이 아니라면 패스하는 로직 추가
        // TODO: 인증, 인가가 필요 없는 API 요청이라면 패스하는 로직 추가
        // TODO: try-catch 추가 필요

        String token = resolveToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            Claims claims = jwtUtils.parseToken(token);
            Long id = claims.get("id", Long.class);
            String email = claims.getSubject();
            String nickname = claims.get("nickname", String.class);
            if (nickname == null) nickname = "";

            SecurityUser user = new SecurityUser(
                    id,
                    email,
                    "",
                    nickname,
                    List.of()
            );
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 객체(Header 또는 Cookie)에서 JWT 토큰을 추출합니다.
     * @param request HTTP 요청 객체
     * @return 추출된 토큰 문자열, 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        // Authorization Header 확인
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}