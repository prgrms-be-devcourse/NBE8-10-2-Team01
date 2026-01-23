package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.service.ProfileImageService;
// ↓ 팀에서 사용하는 공통 응답 객체 (import 경로 확인 필요)
import com.plog.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation; // 스웨거 쓴다면 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members") // URL은 'users'나 'members' 등 팀 규칙에 맞게
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
        
        // 200 OK or 201 Created (수정 개념이 강하면 200, 생성 개념이면 201)
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
