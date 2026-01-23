package com.plog.domain.member.dto;


import lombok.Builder;

/**
 * 인증 성공 시 사용자에게 반환되는 데이터 구조입니다.
 * <p>
 * Access Token을 응답 헤더뿐만 아니라 바디에 중복 포함하여,
 * 프론트엔드 환경에서 토큰 추출 및 상태 관리를 용이하게 돕습니다.
 *
 * @param nickname 인증된 사용자의 닉네임
 * @param accessToken 이후 요청에 사용될Bearer 인증 토큰
 */

@Builder
public record AuthInfoRes(
        String nickname,
        String accessToken
) {
    public static AuthInfoRes from(AuthLoginResult res) {
        return AuthInfoRes.builder()
                .nickname(res.nickname())
                .accessToken(res.accessToken())
                .build();
    }
}