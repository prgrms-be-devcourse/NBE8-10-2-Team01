package com.plog.domain.member.dto;

import lombok.Builder;

/**
 * 사용자 정보를 변경할 때 사용되는 request dto 입니다. 이는, 클라이언트로부터 오는 데이터를
 * 직렬화하여 저장합니다.
 *
 * @author jack8
 * @since 2026-01-18
 */
@Builder
public record MemberUpdaterReq(
        String nickname
) {
}
