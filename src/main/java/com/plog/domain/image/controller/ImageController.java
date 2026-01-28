package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ImageUploadRes;
import com.plog.domain.image.service.ImageService;
import com.plog.global.response.CommonResponse;
import com.plog.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 이미지 리소스와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 이미지 파일을 전송받아 서비스 계층으로 전달하고,
 * 처리 결과를 표준 응답 포맷({@link CommonResponse})으로 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code @RestController}가 적용되어 모든 메서드의 반환값이 Response Body로 직렬화됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageController(ImageService imageService)} <br>
 * {@code @RequiredArgsConstructor}를 통해 서비스 빈을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-20
 */
@RestController
@RequestMapping(value = "/api/images", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 단일 이미지를 업로드합니다.
     * <p>
     * <b>API:</b> [POST] /api/images <br>
     * <b>Content-Type:</b> multipart/form-data
     *
     * @param file 업로드할 이미지 파일 (key: "file")
     * @return 200 OK 상태 코드와 함께 업로드된 이미지 URL을 반환
     */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ImageUploadRes>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal SecurityUser securityUser) {

        Long memberId = securityUser.getId();

            ImageUploadRes result = imageService.uploadImage(file, memberId);

            return ResponseEntity.ok(
                    CommonResponse.success(result, "이미지 업로드 성공")
            );

    }
    /**
     * 다중 이미지를 업로드합니다.
     * <p>
     * <b>API:</b> [POST] /api/images/bulk <br>
     * <b>Content-Type:</b> multipart/form-data
     *
     * @param files 업로드할 이미지 파일 리스트 (key: "files")
     * @return 200 OK 상태 코드와 함께 성공 URL 및 실패 파일명 목록을 반환
     */
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ImageUploadRes>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal SecurityUser securityUser) {

        Long memberId = securityUser.getId();

            ImageUploadRes result = imageService.uploadImages(files, memberId);

            String message = result.failedFilenames().isEmpty()
                    ? "다중 이미지 업로드 성공"
                    : String.format("다건 업로드 완료 (성공: %d건, 실패: %d건)",
                    result.successUrls().size(),
                    result.failedFilenames().size());

            return ResponseEntity.ok(CommonResponse.success(result, message));

    }
}