package com.plog.domain.member.service;

import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.dto.UpdateMemberReq;

/**
 * 사용자에 대한 비지니스 로직을 정의한 service 레이어의 인터페이스입니다.
 * <p>
 * 메서드를 정의하고, 해당 메서드에 대한 명세를 주석으로 작성합니다.
 *
 * <p><b>상속 정보:</b><br>
 * MemberServiceImpl 의 부모 인터페이스
 *
 * @author jack8
 * @see MemberServiceImpl
 * @since 2026-01-18
 */
public interface MemberService {

    /**
     * 회원 ID를 기준으로 사용자 정보를 조회합니다.
     *
     * @param id 조회할 회원의 고유 식별자
     * @return 조회된 회원 정보
     * @throws IllegalArgumentException id가 null 인 경우
     * @throws com.plog.global.exception.exceptions.AuthException
     *         해당 ID에 대한 회원이 존재하지 않는 경우
     */
    MemberInfoRes findMemberWithId(Long id);

    /**
     * 닉네임을 기준으로 사용자 정보를 조회합니다.
     *
     * @param nickname 조회할 회원의 닉네임
     * @return 조회된 회원 정보
     * @throws IllegalArgumentException nickname이 null 이거나 빈 값인 경우
     * @throws com.plog.global.exception.exceptions.AuthException
     *         해당 닉네임에 대한 회원이 존재하지 않는 경우
     */
    MemberInfoRes findMemberWithNickname(String nickname);

    /**
     * 회원 정보를 수정합니다.
     *
     * <p>수정 대상은 회원 기본 정보이며,
     * {@link UpdateMemberReq}에 포함된 값만 변경됩니다.
     *
     * @param memberId 수정할 회원의 고유 식별자
     * @param dto 수정할 회원 정보
     * @return 수정된 회원 정보
     * @throws IllegalArgumentException memberId 또는 dto가 null 인 경우
     * @throws com.plog.global.exception.exceptions.AuthException
     *         해당 회원이 존재하지 않는 경우
     */
    MemberInfoRes updateMemberInfo(Long memberId, UpdateMemberReq dto);
}
