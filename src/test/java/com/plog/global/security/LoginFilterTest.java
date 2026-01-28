package com.plog.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plog.domain.member.dto.AuthSignInReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.global.exception.errorCode.AuthErrorCode;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 로그인 인증을 담당하는 {@link LoginFilter}의 핵심 로직을 검증하는 테스트 클래스입니다.
 * <p>
 * HTTP 요청 바디에서 JSON 데이터를 추출하여 스프링 시큐리티의 인증 토큰으로 변환하고,
 * {@link AuthenticationManager}에게 인증을 올바르게 위임하는지 확인합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 없음. {@code WebMvcTestSupport}를 상속하지 않은 이유는 해당 유틸이 {@code @WebMvcTest} 기반의 슬라이스 테스트를 전제로 하기 때문입니다.
 * 필터 내부의 독립적인 로직 검증을 위해 스프링 컨텍스트를 배제했습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code LoginFilter(AuthenticationManager, ObjectMapper, JwtUtils, ...)} <br>
 * 테스트 대상인 필터가 필드 주입이 아닌 생성자 주입 방식을 사용하므로,
 * {@code @BeforeEach} 단계에서 Mock 객체들을 수동으로 주입하여 인스턴스를 생성합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Mockito (객체 모킹 및 행위 검증)
 *
 * @author minhee
 * @since 2026-01-23
 */

@ExtendWith(MockitoExtension.class)
class LoginFilterTest {

    private LoginFilter loginFilter;
    @Mock private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Spy
    private TokenResolver tokenResolver = new TokenResolver(3600000, "localhost", false);

    @BeforeEach
    void setUp() {
        loginFilter = new LoginFilter(authenticationManager, objectMapper, jwtUtils, tokenResolver);
    }

    @Test
    @DisplayName("인증 시도 - 요청 바디의 JSON을 AuthSignInReq로 변환하여 인증을 위임")
    void attemptAuthentication_success() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        AuthSignInReq signInReq = new AuthSignInReq("test@plog.com", "password123!");
        request.setContent(objectMapper.writeValueAsBytes(signInReq));

        // when
        loginFilter.attemptAuthentication(request, new MockHttpServletResponse());

        // then
        verify(authenticationManager).authenticate(argThat(auth ->
                auth.getPrincipal().equals("test@plog.com") &&
                        auth.getCredentials().equals("password123!")
        ));
    }

    @Test
    @DisplayName("인증 성공 - 토큰 생성 및 헤더/쿠키 설정 확인")
    void successfulAuthentication_test() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        SecurityUser securityUser = mock(SecurityUser.class);
        given(securityUser.getId()).willReturn(1L);
        given(securityUser.getEmail()).willReturn("test@plog.com");
        given(securityUser.getNickname()).willReturn("plogger");

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(securityUser);

        given(jwtUtils.createAccessToken(any(MemberInfoRes.class))).willReturn("mock-access-token");
        given(jwtUtils.createRefreshToken(anyString())).willReturn("mock-refresh-token");

        // when
        loginFilter.successfulAuthentication(request, response, filterChain, authentication);

        // then
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer mock-access-token");

        assertThat(response.getCookie("refreshToken")).isNotNull();
        assertThat(response.getCookie("refreshToken").getValue()).isEqualTo("mock-refresh-token");
        assertThat(response.getCookie("refreshToken").isHttpOnly()).isTrue();

        String content = response.getContentAsString();
        assertThat(content).contains("\"id\":1");
        assertThat(content).contains("plogger님 환영합니다.");
        assertThat(content).contains("mock-access-token");
    }

    @Test
    @DisplayName("인증 실패 - 401 상태코드와 실패 메시지 반환 확인")
    void unsuccessfulAuthentication_test() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException failed = mock(AuthenticationException.class);

        // when
        loginFilter.unsuccessfulAuthentication(request, response, failed);

        // then
        assertThat(response.getStatus()).isEqualTo(401);

        String expectedMessage = AuthErrorCode.INVALID_CREDENTIALS.getMessage();
        String content = response.getContentAsString();
        assertThat(content).contains("fail");
        assertThat(content).contains(expectedMessage);
    }
}