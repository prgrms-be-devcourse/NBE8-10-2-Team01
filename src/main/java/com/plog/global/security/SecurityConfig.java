package com.plog.global.security;


import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.response.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * 애플리케이션의 보안 정책을 설정하는 중앙 설정 클래스입니다.
 * <p>
 * HTTP 요청에 대한 보안 필터 체인을 정의하며, JWT 인증 방식에 맞게
 * 세션 정책을 STATELESS로 설정하고 CORS/CSRF 등 보안 옵션을 조정합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 별도의 상속 없이 Spring Security 6.x 버전의 컴포넌트 기반 설정을 따릅니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code SecurityConfig(CustomAuthenticationFilter customAuthenticationFilter)}<br>
 * 작성된 사용자 정의 인증 필터를 주입받아 필터 체인에 등록합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link Configuration}으로 등록되어 보안 관련 Bean(SecurityFilterChain, PasswordEncoder 등)을 생성 및 관리합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Security, BCryptPasswordEncoder 등을 사용하여 보안 기능을 구현합니다.
 *
 * @author minhee
 * @since 2026-01-16
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final JwtUtils jwtUtils;
    private final List<String> allowedOrigins;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenResolver tokenResolver;

    public SecurityConfig(
            CustomAuthenticationFilter customAuthenticationFilter,
            ObjectMapper objectMapper,
            JwtUtils jwtUtils,
            AuthenticationConfiguration authenticationConfiguration,
            @Value("${custom.cors.allowed-origins}") List<String> allowedOrigins, TokenResolver tokenResolver) {
        this.customAuthenticationFilter = customAuthenticationFilter;
        this.objectMapper = objectMapper;
        this.jwtUtils = jwtUtils;
        this.authenticationConfiguration = authenticationConfiguration;
        this.allowedOrigins = allowedOrigins;
        this.tokenResolver = tokenResolver;
    }

    private LoginFilter loginFilter() throws Exception {
        return new LoginFilter(
                authenticationManager(authenticationConfiguration),
                objectMapper,
                jwtUtils,
                tokenResolver
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(AccessURL.PUBLIC.getUrls().toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(
                        headers -> headers
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .csrf(AbstractHttpConfigurer::disable) // REST API이므로 CSRF 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 로그인창 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
                .addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class) // 로그인 처리 필터
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint(this::handleAuthEntryPoint)
                                .accessDeniedHandler(this::handleAccessDenied)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 인증되지 않은 사용자가 보호된 리소스에 접근했을 때의 처리를 담당합니다.
     * {@link CustomAuthenticationFilter}에서 설정한 "exception" 속성을 확인하여
     * JWT 만료, 유효하지 않은 토큰 등의 구체적인 인증 실패 원인을 파악합니다.
     * <p><b>결과 반환:</b><br>
     * - 속성이 없을 경우: {@link AuthErrorCode#LOGIN_REQUIRED} (401 Unauthorized) 반환 <br>
     * - 구체적 에러가 있을 경우: 해당 에러 코드에 맞는 HTTP 상태와 메시지를 JSON 형식으로 반환
     */
    private void handleAuthEntryPoint(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        AuthErrorCode errorCode = (AuthErrorCode) request.getAttribute("exception");

        if (errorCode == null) {
            errorCode = AuthErrorCode.LOGIN_REQUIRED;
        }

        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.fail(errorCode.getMessage())));
    }

    /**
     * 권한이 없는 사용자가 리소스에 접근했을 때의 처리를 담당합니다.
     * <p><b>역할:</b><br>
     * 인가(Authorization) 실패 시 클라이언트에게 권한 부족을 알리는 공통 응답을 생성합니다.
     * <p><b>결과 반환:</b><br>
     * HTTP 403 Forbidden 상태 코드와 함께 {@link AuthErrorCode#USER_AUTH_FAIL} 메시지를 JSON으로 반환합니다.
     */
    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                    AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.fail(
                                AuthErrorCode.USER_AUTH_FAIL.getMessage())));
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 정의하는 Bean입니다.
     * 허용 오리진: 외부 설정 파일(application.yml)을 통해 주입받습니다.
     * @return 설정이 완료된 CORS 구성 소스 객체
     * @see CorsConfiguration
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정 -> 필요 시 수정
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));

        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));

        // CORS 설정을 소스에 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}