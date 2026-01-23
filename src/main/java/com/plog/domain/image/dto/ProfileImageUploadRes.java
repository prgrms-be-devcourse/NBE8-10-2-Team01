package com.plog.domain.image.dto;

import com.plog.domain.member.entity.Member;

/**
 * 프로필 이미지 업로드/조회 작업 완료 후 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * 특정 회원의 ID와 변경된(또는 조회된) 프로필 이미지의 접근 URL을 포함합니다.
 * 프로필 이미지가 없는 경우 {@code profileImageUrl}은 null을 반환합니다.
 *
 * <p><b>주요 필드:</b><br>
 * {@code memberId}: 프로필 이미지의 소유자인 회원 ID<br>
 * {@code profileImageUrl}: 접근 가능한 전체 이미지 URL (이미지가 없으면 null)
 *
 * <p><b>사용처:</b><br>
 * 프로필 이미지 업로드(POST), 조회(GET) API의 응답 바디로 사용됩니다.
 *
 * @param memberId 회원 고유 ID
 * @param profileImageUrl 프로필 이미지 URL (http://...)
 * @author Jaewon Ryu
 * @since 2026-01-23
 * @see com.plog.domain.image.controller.ProfileImageController
 */
public record ProfileImageUploadRes(
                                      Long memberId,
                                      String profileImageUrl
) {
    public static ProfileImageUploadRes from(Member member) {
        String imageUrl = (member.getProfileImage() != null)
                ? member.getProfileImage().getAccessUrl()
                : null;

        return new ProfileImageUploadRes(member.getId(), imageUrl);
    }
}