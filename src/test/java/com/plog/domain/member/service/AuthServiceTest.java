package com.plog.domain.member.service;

import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * {@link AuthServiceImpl} 에 대한 단위 테스트 입니다.
 *
 * @author minhee
 * @since 2026-01-21
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공 - 비밀번호를 암호화 저장, ID를 반환")
    void signUp_success() {
        // given
        String email = "test@plog.com";
        String rawPassword = "password123!";
        String encodedPassword = "encoded_password";
        String nickname = "plogger";

        Member mockMember = mock(Member.class);
        given(mockMember.getId()).willReturn(1L);

        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(mockMember);

        // when
        Long savedId = authService.signUp(email, rawPassword, nickname);

        // then
        assertThat(savedId).isEqualTo(1L);
        then(memberRepository).should(times(1)).findByEmail(email);
        then(passwordEncoder).should(times(1)).encode(rawPassword);
        then(memberRepository).should(times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일, USER_ALREADY_EXIST 예외 발생")
    void signUp_fail_alreadyExist() {
        // given
        String email = "exist@plog.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(mock(Member.class)));

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.signUp(email, "pw", "nick"));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_EXIST);
        then(memberRepository).should(times(1)).findByEmail(email);
        then(memberRepository).should(times(0)).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 성공 - 회원 정보 반환")
    void signIn_success() {
        // given
        String email = "test@plog.com";
        String password = "password123!";
        String encodedPassword = "encoded_password";
        Member mockMember = Member.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);

        // when
        Member result = authService.signIn(email, password);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        then(passwordEncoder).should(times(1)).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치, INVALID_CREDENTIALS 예외 발생")
    void signIn_fail_invalidPassword() {
        // given
        String email = "test@plog.com";
        String password = "wrong_password";
        Member mockMember = Member.builder()
                .email(email)
                .password("encoded_password")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when
        AuthException ex = assertThrows(AuthException.class, () -> authService.signIn(email, password));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("AccessToken 생성 시 JwtUtils를 호출")
    void genAccessToken_success() {
        // given
        Member mockMember = mock(Member.class);
        given(mockMember.getId()).willReturn(1L);
        given(mockMember.getEmail()).willReturn("test@plog.com");
        given(mockMember.getNickname()).willReturn("nick");

        given(jwtUtils.createAccessToken(anyMap())).willReturn("mock-token");

        // when
        String token = authService.genAccessToken(mockMember);

        // then
        assertThat(token).isEqualTo("mock-token");
        then(jwtUtils).should(times(1)).createAccessToken(anyMap());
    }

    @Test
    @DisplayName("토큰 재발급 성공 - 리프레시 토큰이 유효하면 새 AccessToken 발급")
    void accessTokenReissue_success() {
        // given
        String refreshToken = "valid-refresh-token";
        Long memberId = 1L;
        Member mockMember = mock(Member.class);
        Claims mockClaims = mock(Claims.class);

        given(jwtUtils.parseToken(refreshToken)).willReturn(mockClaims);
        given(mockClaims.get("id", Long.class)).willReturn(memberId);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));

        // genAccessToken 내부 로직 모킹
        given(mockMember.getId()).willReturn(memberId);
        given(mockMember.getEmail()).willReturn("test@plog.com");
        given(mockMember.getNickname()).willReturn("nick");
        given(jwtUtils.createAccessToken(anyMap())).willReturn("new-access-token");

        // when
        String resultToken = authService.accessTokenReissue(refreshToken);

        // then
        assertThat(resultToken).isEqualTo("new-access-token");
        then(jwtUtils).should(times(1)).parseToken(refreshToken);
        then(memberRepository).should(times(1)).findById(memberId);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료 시 LOGIN_REQUIRED 예외 발생")
    void accessTokenReissue_fail_expired() {
        // given
        String expiredToken = "expired-token";
        // parseToken 호출 시 ExpiredJwtException이 발생하도록 설정
        given(jwtUtils.parseToken(expiredToken)).willThrow(
                new io.jsonwebtoken.ExpiredJwtException(null, null, "token expired")
        );

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.accessTokenReissue(expiredToken));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.LOGIN_REQUIRED);
        assertThat(ex.getMessage()).isEqualTo("세션이 만료되었습니다. 다시 로그인해 주세요.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 토큰이 null이면 TOKEN_INVALID 예외 발생")
    void accessTokenReissue_fail_nullToken() {
        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.accessTokenReissue(null));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.TOKEN_INVALID);
    }
}