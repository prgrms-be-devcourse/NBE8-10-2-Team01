package com.plog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 게시물 수정을 위해 클라이언트로부터 전달받는 요청 데이터 레코드입니다.
 * <p>
 * HTTP Request Body의 JSON 데이터를 자바 객체로 바인딩하며,
 * Jakarta Validation 어노테이션을 통해 입력 데이터의 유효성을 1차적으로 검증합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link java.lang.Record} 클래스를 암시적으로 상속받으며, 불변(Immutable) 객체입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostUpdateReq(String title, String content, String thumbnail, List<String> hashtags)} <br>
 * 모든 필드를 초기화하는 컴팩트 생성자가 자동으로 정의됩니다.
 *
 * <p><b>빈 관리:</b><br>
 * 스프링 빈으로 관리되지 않으며, Jackson 라이브러리에 의해 직렬화/역직렬화 시점에 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Jakarta Validation(@NotBlank)을 사용하여 유효성 검사 규칙을 정의합니다.
 *
 * @author MintyU
 * @see com.plog.domain.post.controller.PostController
 * @since 2026-01-20
 */
public record PostUpdateReq(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        String title,

        @NotBlank(message = "본문은 필수 입력 항목입니다.")
        String content,

        List<String> hashtags,

        String thumbnail
) {
}
