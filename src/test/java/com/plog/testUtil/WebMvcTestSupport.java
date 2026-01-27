package com.plog.testUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plog.domain.member.service.AuthService;
import com.plog.global.security.CustomUserDetailsService;
import com.plog.global.security.JwtUtils;
import com.plog.global.security.TokenResolver;
import com.plog.global.security.TokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * WebMvcTest 에 대한 테스트 유틸입니다.
 * <p>
 * autowired 된 MockMvc 및 사용할 Objectmapper 를 사전에 정의합니다.
 * 보안 필터가 요구하는 공통 보안 빈들을 Mock 객체로 등록합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 모든 slice 테스트의 부모 추상 클래스입니다.
 *
 *
 * @author jack8
 * @since 2026-01-20
 */
public abstract class WebMvcTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    protected JwtUtils jwtUtils;

    @MockitoBean
    protected TokenResolver tokenResolver;

    @MockitoBean
    protected TokenStore tokenStore;

    @MockitoBean
    protected AuthenticationConfiguration authenticationConfiguration;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

}