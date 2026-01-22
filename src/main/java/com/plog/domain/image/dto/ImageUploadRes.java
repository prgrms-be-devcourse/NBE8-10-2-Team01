package com.plog.domain.image.dto;

import java.util.List;

/**
 * 이미지 업로드 작업 완료 후 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * 단건/다건 업로드 모두 지원하며, 성공한 이미지 URL과 실패한 파일명(선택적)을 제공합니다.
 * 실패가 없을 때는 {@code failedFilenames}가 비어있으므로 기존 클라이언트와 호환됩니다.
 *
 * <p><b>주요 필드:</b><br>
 * {@code successUrls}: 성공한 이미지의 접근 URL 리스트 (순서 보장)<br>
 * {@code failedFilenames}: 실패한 파일명 리스트 (없으면 빈 리스트)
 *
 * <p><b>호환성:</b><br>
 * 기존 단건 업로드 클라이언트는 {@code successUrls}만 사용하므로 변경 없이 동작합니다.
 *
 * @param successUrls 성공한 이미지의 접근 URL 리스트
 * @param failedFilenames 실패한 파일명 리스트 (없으면 빈 리스트)
 * @author Jaewon Ryu
 * @since 2026-01-20
 * @see com.plog.domain.image.controller.ImageController
 */

public record ImageUploadRes(
        List<String> successUrls,
        List<String> failedFilenames
    ) {
}
