package com.plog.domain.member.service;

import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

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
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공 - 이메일/닉네임 중복 없음, 비밀번호를 암호화, ID 반환")
    void signUp_success() {
        // given
        AuthSignUpReq req = new AuthSignUpReq("test@plog.com", "password123!", "plogger");
        String encodedPassword = "encoded_password";

        Member mockMember = mock(Member.class);
        given(mockMember.getId()).willReturn(1L);

        given(memberRepository.existsByEmail(req.email())).willReturn(false);
        given(memberRepository.existsByNickname(req.nickname())).willReturn(false);
        given(passwordEncoder.encode(req.password())).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(mockMember);

        // when
        Long savedId = authService.signUp(req);

        // then
        assertThat(savedId).isEqualTo(1L);
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signUp_fail_alreadyExist() {
        // given
        String email = "exist@plog.com";
        AuthSignUpReq req = new AuthSignUpReq(email, "pw", "nick");
        given(memberRepository.existsByEmail(email)).willReturn(true);

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.signUp(req));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_EXIST);
        assertThat(ex.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 사용 중인 닉네임")
    void signUp_fail_alreadyExistNickname() {
        // given
        String email = "test@plog.com";
        String nickname = "duplicateNick";
        AuthSignUpReq req = new AuthSignUpReq(email, "pw", nickname);

        given(memberRepository.existsByEmail(email)).willReturn(false);
        given(memberRepository.existsByNickname(nickname)).willReturn(true);

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.signUp(req));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_EXIST);
        assertThat(ex.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원 ID 조회 성공 - 존재하는 ID일 경우 정보를 반환")
    void findMemberWithId_success() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .email("test@plog.com")
                .nickname("plogger")
                .build();
        ReflectionTestUtils.setField(member, "id", memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        MemberInfoRes result = authService.findMemberWithId(memberId);

        // then
        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.email()).isEqualTo("test@plog.com");
    }

    @Test
    @DisplayName("회원 ID 조회 실패 - 존재하지 않는 ID일 경우 AuthException 발생")
    void findMemberWithId_fail() {
        // given
        Long invalidId = 999L;
        given(memberRepository.findById(invalidId)).willReturn(Optional.empty());

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.findMemberWithId(invalidId));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_NOT_FOUND);
        assertThat(ex.getLogMessage()).contains("[AuthServiceImpl#findMemberWithId]");
    }
}