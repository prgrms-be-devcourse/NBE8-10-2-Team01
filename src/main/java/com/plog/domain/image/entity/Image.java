package com.plog.domain.image.entity;

import com.plog.domain.member.entity.Member;
import com.plog.domain.post.entity.Post;
import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글에 첨부되는 이미지 파일의 메타데이터를 관리하는 JPA 엔티티입니다.
 * <p>
 * 물리적 파일은 Object Storage(MinIO)에 저장하고, DB에는 해당 파일의
 * 원본명, 저장된 키 값(Stored Name), 접근 URL 등 메타데이터만 보관합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 생성일시(createdAt)와 수정일시(updatedAt)를 자동으로 관리합니다.
 *
 * <p><b>생명주기 관리:</b><br>
 * 스프링 빈이 아니며, JPA 영속성 컨텍스트(Persistence Context)에 의해 생명주기가 관리됩니다.
 *
 * <p><b>주요 패턴:</b><br>
 * {@code @Builder}를 통해 객체 생성 시 가독성을 높이고,
 * {@code @NoArgsConstructor(access = AccessLevel.PROTECTED)}로 무분별한 기본 생성자 호출을 방지합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-20
 * @see
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Image extends BaseEntity {

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storedName;

    @Column(nullable = false)
    private String accessUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member uploader;
    }