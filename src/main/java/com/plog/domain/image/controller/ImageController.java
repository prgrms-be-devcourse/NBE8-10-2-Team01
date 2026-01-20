package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ImageUploadRes;
import com.plog.domain.image.service.ImageService;
import com.plog.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageController(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-20
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ImageUploadRes>> uploadImage(
            @RequestPart("file") MultipartFile file
    ) {
        String imageUrl = imageService.uploadImage(file);

        ImageUploadRes resDto = new ImageUploadRes(List.of(imageUrl));

        return ResponseEntity.ok(
                CommonResponse.success(resDto,"이미지 업로드 성공")
        );
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ImageUploadRes>> uploadImages(
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<String> imageUrls = imageService.uploadImages(files);

        ImageUploadRes resDto = new ImageUploadRes(imageUrls);

        return ResponseEntity.ok(
                CommonResponse.success(resDto,"다중 이미지 업로드 성공")
        );
    }
}