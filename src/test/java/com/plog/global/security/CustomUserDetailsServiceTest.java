package com.plog.global.security;

import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * {@link CustomUserDetailsService}에 대한 단위 테스트입니다.
 * <p>
 * DB에서 사용자 정보를 조회하여 스프링 시큐리티 전용 객체인 {@link SecurityUser}로
 * 올바르게 변환하는지 검증합니다.
 *
 * <p><b>테스트 전략:</b><br>
 * 컨벤션에 따라 스프링 컨텍스트를 로드하지 않고 Mockito만을 사용하여
 * 레포지토리와의 의존성을 분리한 순수 단위 테스트를 수행합니다.
 *
 * @author minhee
 * @since 2026-01-23
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("사용자 조회 성공 - 이메일이 존재하면 SecurityUser를 반환")
    void loadUserByUsername_success() {
        // given
        String email = "test@plog.com";
        Member member = Member.builder()
                .email(email)
                .password("encoded_password")
                .nickname("plogger")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        // when
        SecurityUser userDetails = (SecurityUser) userDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getNickname()).isEqualTo("plogger");
        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getPassword()).isEqualTo("encoded_password");
    }

    @Test
    @DisplayName("사용자 조회 실패 - 이메일이 없으면 AuthException(USER_NOT_FOUND)이 발생")
    void loadUserByUsername_fail_notFound() {
        // given
        String email = "non-exist@plog.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> userDetailsService.loadUserByUsername(email));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_NOT_FOUND);
        assertThat(ex.getLogMessage()).contains("[CustomUserDetailsService#loadUserByUsername]");
        assertThat(ex.getMessage()).isEqualTo("존재하지 않는 사용자입니다.");
    }
}