package com.plog.domain.member.service;

import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.dto.MemberUpdaterReq;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * {@link MemberService} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2026-01-19
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Member member;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void findMemberWithId_success() {
        //given
        Long userId = 1L;
        given(memberRepository.findById(userId)).willReturn(Optional.of(member));
        given(member.getId()).willReturn(userId);

        //when
        MemberInfoRes response = memberService.findMemberWithId(userId);

        //then
        assertThat(response.id()).isEqualTo(userId);
    }

    @Test
    void findMemberWithId_fail_userNotFound() {
        //given
        Long userId = 1L;
        given(memberRepository.findById(userId)).willReturn(Optional.empty());

        //when
        AuthException ex = assertThrows(AuthException.class,
                () -> memberService.findMemberWithId(userId));

        //then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_NOT_FOUND);
        assertThat(ex.getLogMessage()).contains("can't find user by id");
    }

    @Test
    void findMemberWithNickname_success() {
        // given
        String nickname = "jack";
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(member));
        given(member.getId()).willReturn(1L);

        // when
        MemberInfoRes response = memberService.findMemberWithNickname(nickname);

        // then
        assertThat(response.id()).isEqualTo(1L);
        then(memberRepository).should(times(1)).findByNickname(nickname);
    }

    @Test
    void findMemberWithNickname_fail_userNotFound() {
        // given
        String nickname = "jack";
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.empty());

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> memberService.findMemberWithNickname(nickname));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_NOT_FOUND);
        assertThat(ex.getLogMessage()).contains("can't find user by nickname");
        then(memberRepository).should(times(1)).findByNickname(nickname);
    }

    @Test
    void updateMemberInfo_success() {
        // given
        Long memberId = 1L;
        MemberUpdaterReq dto = new MemberUpdaterReq("newNick");

        // 조회는 기존 member로
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // update는 "업데이트된 member"를 리턴한다고 가정
        Member updatedMember = mock(Member.class);
        given(member.update(dto.nickname())).willReturn(updatedMember);

        // save는 보통 void or 반환(member)인데 둘 다 대응 가능
        given(memberRepository.save(updatedMember)).willReturn(updatedMember);

        // DTO 변환에서 id를 검증할 거면 updatedMember.getId()가 필요
        given(updatedMember.getId()).willReturn(memberId);

        // when
        MemberInfoRes response = memberService.updateMemberInfo(memberId, dto);

        // then
        assertThat(response.id()).isEqualTo(memberId);

        then(memberRepository).should(times(1)).findById(memberId);
        then(member).should(times(1)).update("newNick");
        then(memberRepository).should(times(1)).save(updatedMember);
    }

    @Test
    void updateMemberInfo_fail_userNotFound() {
        // given
        Long memberId = 1L;
        MemberUpdaterReq dto = new MemberUpdaterReq("newNick");
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when
        AuthException ex = assertThrows(AuthException.class,
                () -> memberService.updateMemberInfo(memberId, dto));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.USER_NOT_FOUND);
        assertThat(ex.getLogMessage()).contains("can't find user by id");

        then(memberRepository).should(times(1)).findById(memberId);
        then(memberRepository).should(never()).save(any());
        then(member).shouldHaveNoInteractions(); // member는 조회 자체가 안 됐으니 상호작용 없어야 정상
    }

    @Test
    void isDuplicateEmail_true() {
        // given
        String email = "example@email.com";
        given(memberRepository.existsByEmail(email)).willReturn(true);

        // when
        boolean result = memberService.isDuplicateEmail(email);

        // then
        assertThat(result).isTrue();
        then(memberRepository).should(times(1)).existsByEmail(email);
    }

    @Test
    void isDuplicateEmail_false() {
        // given
        String email = "example@email.com";
        given(memberRepository.existsByEmail(email)).willReturn(false);

        // when
        boolean result = memberService.isDuplicateEmail(email);

        // then
        assertThat(result).isFalse();
        then(memberRepository).should(times(1)).existsByEmail(email);
    }

    @Test
    void isDuplicateNickname_true() {
        // given
        String nickname = "jack";
        given(memberRepository.existsByNickname(nickname)).willReturn(true);

        // when
        boolean result = memberService.isDuplicateNickname(nickname);

        // then
        assertThat(result).isTrue();
        then(memberRepository).should(times(1)).existsByNickname(nickname);
    }

    @Test
    void isDuplicateNickname_false() {
        // given
        String nickname = "jack";
        given(memberRepository.existsByNickname(nickname)).willReturn(false);

        // when
        boolean result = memberService.isDuplicateNickname(nickname);

        // then
        assertThat(result).isFalse();
        then(memberRepository).should(times(1)).existsByNickname(nickname);
    }

}