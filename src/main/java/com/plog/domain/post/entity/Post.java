package com.plog.domain.post.entity;

import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 블로그 게시물의 핵심 데이터를 담당하는 엔티티 클래스입니다.
 * <p>
 * 마크다운 형식의 본문과 검색 최적화를 위한 순수 텍스트, 요약글 등을 관리하며,
 * JPA를 통해 MySQL의 MEDIUMTEXT 타입과 매핑되어 대용량 텍스트를 저장합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 고유 식별자(id)와 생성/수정 시간을 공통으로 관리합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code @Builder} 패턴을 사용하여 가독성 있게 객체를 생성합니다. <br>
 * JPA 프록시 생성을 위해 {@code protected} 수준의 기본 생성자가 포함되어 있습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Lombok(@Getter, @Builder 등)과 Jakarta Persistence API를 사용합니다.
 *
 * @author MintyU
 * @since 2026-01-15
 * @see BaseEntity
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity{
    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Builder.Default
    private int viewCount = 0;
}
