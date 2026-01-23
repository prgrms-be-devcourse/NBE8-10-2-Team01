package com.plog.global.security;


import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.service.AuthService;
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
 * @see SecurityContextHolder
 * @see JwtUtils
 * @since 2026-01-16
 */

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final TokenResolver tokenResolver;
    private final AuthService authService;

    /**
     * 필터의 핵심 로직을 수행하며, 토큰 유무에 따라 인증 정보를 컨텍스트에 저장합니다.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = tokenResolver.resolveAccessToken(request);

        try {
            if (token != null) {
                authenticate(token);
            }
        } catch (ExpiredJwtException e) {
            handleAccessTokenReissue(request, response);
        } catch (Exception e) {
            request.setAttribute("exception", AuthErrorCode.TOKEN_INVALID);
        }

        filterChain.doFilter(request, response);
    }

    /**
     *
     * @param token
     */
    private void authenticate(String token) {
        Claims claims = jwtUtils.parseToken(token);
        Long id = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);

        SecurityUser user = SecurityUser.securityUserBuilder()
                .id(id)
                .email(email)
                .password("")
                .nickname(nickname != null ? nickname : "")
                .authorities(List.of())
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // TODO: refresh token rotation 추후 도입
    private void handleAccessTokenReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = tokenResolver.resolveRefreshToken(request);

        if (refreshToken != null) {
            Claims claims = jwtUtils.parseToken(refreshToken);
            Long memberId = Long.valueOf(claims.getSubject());

            MemberInfoRes memberInfo = authService.findMemberWithId(memberId);
            String newAccess = jwtUtils.createAccessToken(memberInfo);
            tokenResolver.setHeader(response, newAccess);

            authenticate(newAccess);
        } else {
            request.setAttribute("exception", AuthErrorCode.TOKEN_EXPIRED);
        }
    }
}