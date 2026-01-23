package com.plog.domain.image.service;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 관리를 위한 서비스 인터페이스입니다.
 *
 * @author [Your Name]
 * @since 2026-01-23
 */
public interface ProfileImageService {

    /**
     * 회원의 프로필 이미지를 업로드합니다.
     * 기존 프로필 이미지가 있는 경우 교체됩니다.
     */
    ProfileImageUploadRes uploadProfileImage(Long memberId, MultipartFile file);

    /**
     * 회원의 프로필 이미지를 조회합니다.
     */
    ProfileImageUploadRes getProfileImage(Long memberId);

    /**
     * 회원의 프로필 이미지를 삭제(초기화)합니다.
     */
    void deleteProfileImage(Long memberId);
}
