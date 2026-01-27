package com.plog.domain.comment.repository;

import com.plog.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 댓글(PostComment) 엔티티에 대한 기본적인 CRUD 기능을 제공합니다.
 * </p>
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>댓글 다건 조회 + 페이징 기능</li>
 *   <li>댓글 저장 및 삭제</li>
 *   <li>댓글 수정</li>
 * </ul>
 *
 * @author 노정원
 * @since 2026-01-15
 * @see Comment
 * @see JpaRepository
 */
public interface CommentRepository extends JpaRepository<Comment, Long>{

    @EntityGraph(attributePaths = {"author"}) // N+1 방지
    Slice<Comment> findByPostIdAndParentIsNull(Long postId, Pageable pageable);

    @Query("select c from Comment c " +
            "join fetch c.author m " +             // Member 조인
            "left join fetch m.profileImage i " +  // Member의 ProfileImage 조인 (이미지 없는 유저 고려)
            "where c.post.id = :postId and c.parent is null " +
            "order by c.createDate desc")
    Slice<Comment> findCommentsWithMemberAndImageByPostId(@Param("postId") Long postId, Pageable pageable);

    // 대댓글 조회용 (추가)
    @EntityGraph(attributePaths = {"author"})
    Slice<Comment> findByParentId(Long parentId, Pageable pageable);

    @Query("select r from Comment r " +
            "join fetch r.author m " +                // 대댓글 작성자 조인
            "left join fetch m.profileImage i " +     // 작성자의 프로필 이미지 조인
            "where r.parent.id = :parentId " +        // 특정 댓글의 자식들만 조회
            "order by r.createDate asc")              // 대댓글은 보통 오래된 순(작성순)으로 정렬
    Slice<Comment> findRepliesWithMemberAndImageByParentId(@Param("parentId") Long parentId, Pageable pageable);


    boolean existsByParent(Comment parent);
}
