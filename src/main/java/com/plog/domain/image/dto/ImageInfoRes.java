package com.plog.domain.image.dto;

import com.plog.domain.image.entity.Image;  // ✅ Image (Entity)
import java.time.LocalDateTime;

/**
 * 이미지 상세 정보를 클라이언트에게 전달하기 위한 응답 데이터 레코드입니다.
 * <p>
 * 데이터베이스 엔티티({@link Image})를 직접 노출하지 않고,
 * API 스펙에 필요한 필드만 선택적으로 포함하여 보안성과 유지보수성을 높입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link java.lang.Record}를 암시적으로 상속받으며, 모든 필드는 final입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageInfoRes(Long id, String url, String originalName, LocalDateTime createdAt)}
 *
 * @author Jaewon Ryu
 * @see com.plog.domain.image.entity.Image
 * @since 2026-01-22
 */
public record ImageInfoRes(  // ← record = 간단한 DTO (팀 컨벤션)
                             Long imageId,
                             String originalName,
                             String accessUrl,
                             String storedName
) {

    // Entity(Image) → DTO 변환 (Service에서 사용)
    public static ImageInfoRes from(Image image) {
        return new ImageInfoRes(
                image.getId(),
                image.getOriginalName(),
                image.getAccessUrl(),
                image.getStoredName()
        );
    }
}