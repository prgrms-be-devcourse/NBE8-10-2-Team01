package com.plog.domain.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시물의 발행 상태를 관리하는 enum 클래스입니다.
 * <p>
 * 각 상태 상수는 설명(description)과 결합되어 있으며,
 * DB에는 문자열 형태로 저장되어 가독성을 높입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostStatus(String description)} <br>
 * 상태 상수에 매핑될 한글 설명을 주입받는 생성자입니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Lombok(@Getter, @RequiredArgsConstructor)을 통해 보일러플레이트 코드를 최소화했습니다.
 *
 * @author MintyU
 * @since 2026-01-15
 */

@Getter
@RequiredArgsConstructor
public enum PostStatus {
    DRAFT("임시 저장"),
    PUBLISHED("발행됨"),
    HIDDEN("숨김 처리");

    private final String description;
}
