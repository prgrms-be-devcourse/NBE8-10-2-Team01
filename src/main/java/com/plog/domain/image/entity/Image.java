package com.plog.domain.image.entity;

import com.plog.domain.post.entity.Post;
import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글에 포함되는 이미지 파일의 메타데이터를 관리하는 엔티티입니다.
 * <p>
 * 원본 파일명, S3/MinIO에 저장된 실제 키 값(storedName), 그리고 접근 가능한 URL을 저장합니다.
 * 추후 게시글 삭제 시, 연관된 이미지를 찾아 스토리지에서도 삭제하기 위한 기준 정보가 됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 생성/수정 시간을 관리합니다.
 *
 * <p><b>주요 패턴:</b><br>
 * {@code @Builder}를 사용하여 객체 생성을 유연하게 처리합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-20
 * @see Post
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
    @JoinColumn(name = "post_id")
    private Post post;

    public void setPost(Post post) {
        this.post = post;
    }
}
