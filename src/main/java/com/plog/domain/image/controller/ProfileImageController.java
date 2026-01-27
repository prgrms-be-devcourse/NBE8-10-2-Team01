package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.service.ProfileImageService;
import com.plog.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 리소스와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 프로필 이미지 파일을 전송받아 업로드하거나,
 * 현재 설정된 프로필 이미지를 조회 및 삭제하는 기능을 제공합니다.
 * 처리 결과를 표준 응답 포맷({@link CommonResponse})으로 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code @RestController}가 적용되어 모든 메서드의 반환값이 Response Body로 직렬화됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ProfileImageController(ProfileImageService profileImageService)} <br>
 * {@code @RequiredArgsConstructor}를 통해 서비스 빈을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-23
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Profile Image", description = "프로필 이미지 관련 API")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    /**
     * 프로필 이미지 업로드 (수정)
     * [POST] /api/members/{memberId}/profile-image
     */
    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드하거나 교체합니다.")
    @PostMapping(
        value = "/{memberId}/profile-image", 
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommonResponse<ProfileImageUploadRes>> uploadProfileImage(
            @PathVariable Long memberId,
            @RequestPart("file") MultipartFile file
    ) {
        ProfileImageUploadRes response = profileImageService.uploadProfileImage(memberId, file);

        return ResponseEntity.ok(
            CommonResponse.success(response, "프로필 이미지가 성공적으로 변경되었습니다.")
        );
    }

    /**
     * 프로필 이미지 조회
     * [GET] /api/members/{memberId}/profile-image
     */
    @Operation(summary = "프로필 이미지 조회", description = "사용자의 현재 프로필 이미지 URL을 조회합니다.")
    @GetMapping("/{memberId}/profile-image")
    public ResponseEntity<CommonResponse<ProfileImageUploadRes>> getProfileImage(
            @PathVariable Long memberId
    ) {
        ProfileImageUploadRes response = profileImageService.getProfileImage(memberId);

        return ResponseEntity.ok(
            CommonResponse.success(response, "프로필 이미지를 조회했습니다.")
        );
    }


    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제하고 기본 상태로 되돌립니다.")
    @DeleteMapping("/{memberId}/profile-image")
    public ResponseEntity<CommonResponse<Void>> deleteProfileImage(
            @PathVariable Long memberId
    ) {
        profileImageService.deleteProfileImage(memberId);

        return ResponseEntity.ok(
                CommonResponse.success(null, "프로필 이미지가 삭제되었습니다.")
        );
    }
}
