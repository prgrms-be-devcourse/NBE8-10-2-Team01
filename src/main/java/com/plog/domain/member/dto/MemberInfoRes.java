package com.plog.domain.member.dto;

import com.plog.domain.member.entity.Member;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * member 데이터를 조회할 때, 반환되는 데이터의 기본 형식입니다. 사용자의 기본적인 데이터가 포함되어 있습니다.
 *
 * @author jack8
 * @since 2026-01-18
 */
@Builder
public record MemberInfoRes(
        Long id,
        String email,
        String nickname,
        LocalDateTime createDate
) {

    public static MemberInfoRes from(Member member) {
        return MemberInfoRes.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .createDate(member.getCreateDate())
                .build();
    }
}