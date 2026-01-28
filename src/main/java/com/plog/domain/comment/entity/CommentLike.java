package com.plog.domain.comment.entity;

import com.plog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 특정 댓글({@link Comment})에 대한 회원({@link Member})의 좋아요 상태를 관리하는 엔티티이다.
 * <p>
 * 하나의 회원은 하나의 댓글에 대해 단 한 번의 좋아요만 수행할 수 있도록
 * 데이터베이스 수준에서 복합 유니크 제약 조건(Unique Constraint)을 통해 중복을 방지한다.
 * 서비스 계층의 좋아요 토글(Toggle) 로직의 핵심 데이터 모델로 사용된다.
 *
 * <p><b>상속 정보:</b><br>
 * 공통 감사(Auditing) 정보가 필요하지 않은 단순 관계 엔티티이므로
 * 별도의 BaseEntity를 상속받지 않고 최소한의 식별값만 유지한다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #builder()}를 활용한 전체 필드 생성자를 사용하며,
 * JPA 스펙을 위해 {@code AccessLevel.PROTECTED} 수준의 기본 생성자를 포함한다.
 *
 * <p><b>테이블 제약 조건:</b><br>
 * {@code uk_comment_member}: comment_id와 member_id의 조합을 유니크하게 유지.
 *
 * @author njwwn
 * @see Comment
 * @see Member
 * @since 2026-01-28
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "comment_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_member",
                        columnNames = {"comment_id", "member_id"}
                )
        }
)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}