package com.plog.domain.image.service;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 컨트롤러와 구현체 사이의 결합도를 낮추기 위해 사용되며,
 * 프로필 이미지 업로드, 조회, 삭제에 대한 표준 명세를 제공합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - 프로필 이미지 업로드 및 교체 <br>
 * - 프로필 이미지 URL 조회 <br>
 * - 프로필 이미지 삭제 (초기화)
 *
 * @author Jaewon Ryu
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
