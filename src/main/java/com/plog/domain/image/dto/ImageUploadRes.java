package com.plog.domain.image.dto;

import java.util.List;

/**
 * 이미지 업로드 작업 완료 후 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * 업로드된 이미지들의 접근 가능한 URL 정보를 리스트 형태로 캡슐화하여 전달합니다.
 * Java Record 타입을 사용하여 데이터의 불변성(Immutable)을 보장하며,
 * 단일 업로드와 다중 업로드 응답을 이 클래스 하나로 통일하여 처리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code java.lang.Record}를 상속받는 레코드 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageUploadRes(List<String> imageUrls)} <br>
 * 업로드된 이미지의 URL 리스트를 받아 객체를 생성합니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 스프링 빈으로 관리되지 않으며, 요청이 처리될 때마다 생성되어 반환되는 POJO입니다.
 *
 * @param imageUrls 저장소에 업로드된 이미지의 전체 접근 URL 리스트 (순서 보장)
 * @author Jaewon Ryu
 * @see com.plog.domain.image.controller.ImageController
 * @since 2026-01-20
 */

public record ImageUploadRes(
            List<String> imageUrls
    ) {
}
