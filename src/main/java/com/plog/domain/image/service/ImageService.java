package com.plog.domain.image.service;

import com.plog.domain.image.dto.ImageUploadRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 이미지 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 컨트롤러와 구현체 사이의 결합도를 낮추기 위해 사용되며,
 * 이미지 업로드 및 관련 데이터 처리에 대한 표준 명세를 제공합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - 단일 이미지 업로드 및 URL 반환 <br>
 * - 다중 이미지 일괄 업로드 및 URL 리스트 반환
 * - 단일 이미지 삭제 <br>
 * - 다중 이미지 일괄 삭제 <br>
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-20
 */
public interface ImageService {

    /**
     * 단일 이미지를 업로드하고 URL을 반환합니다.
     * DB에 이미지 정보를 저장합니다.
     */
    ImageUploadRes uploadImage(MultipartFile file, Long memberId);


    /**
     * 다중 이미지를 업로드하고 URL 리스트를 반환합니다.
     */
    ImageUploadRes uploadImages(List<MultipartFile> files, Long memberId);


    /**
     * 이미지 URL 하나를 받아 해당 이미지를 삭제합니다. (단일 삭제)
     * Controller에 매핑되지 않은 내부용 메서드입니다.
     */
    void deleteImage(String imageUrl, Long memberId);


    /**
     * 이미지 URL 리스트를 받아 여러 이미지를 한 번에 삭제합니다. (일괄 삭제)
     * Controller에 매핑되지 않은 내부용 메서드입니다.
     */
    void deleteImages(List<String> imageUrls, Long memberId);

}
