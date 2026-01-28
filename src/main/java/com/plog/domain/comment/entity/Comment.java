package com.plog.domain.comment.entity;

import com.plog.domain.post.entity.Post;
import com.plog.global.jpa.entity.BaseEntity;
import com.plog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

/**
 * 게시글에 작성되는 댓글(Comment) 및 대댓글(Reply)을 표현하는 엔티티 클래스이다.
 * <p>
 * 본 엔티티는 게시글({@link Post})과 작성자({@link Member})에 종속되며,
 * 자기 참조 연관관계(parent)를 통해 계층형 댓글 구조를 구성한다.
 * 단일 댓글과 대댓글은 동일한 엔티티로 관리되며,
 * 부모 댓글의 존재 여부에 따라 depth 값이 자동으로 결정된다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 고유 식별자(id)와 생성/수정 시간을 공통으로 관리합니다.
 *
 * <p><b>삭제 정책:</b><br>
 * 댓글은 물리적으로 삭제되지 않고, deleted 플래그를 사용하는
 * 소프트 삭제(Soft Delete) 방식을 따른다.
 * 삭제된 댓글은 서비스 정책에 따라 내용이 치환될 수 있다.
 *
 *
 * @author 노정원
 * @since 2026-01-15
 * @see Post
 * @see Member
 */

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    public void modify(String content){
        this.content = content;
    }

    public void softDelete(){
        this.deleted = true;
        this.content = "[삭제된 댓글입니다.]";
    }

    @Formula("(SELECT count(*) FROM comment c WHERE c.parent_id = id AND c.deleted = false)")
    private long replyCount;
}
