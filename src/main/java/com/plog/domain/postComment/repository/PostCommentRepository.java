package com.plog.domain.postComment.repository;

import com.plog.domain.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 댓글(PostComment) 엔티티에 대한 기본적인 CRUD 기능을 제공합니다.
 * 별도의 구현 클래스 없이도 저장, 조회, 수정, 삭제 기능을 사용할 수 있습니다.
 * </p>
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>댓글 단건 조회 및 전체 조회</li>
 *   <li>댓글 저장 및 삭제</li>
 *   <li>댓글 수정(Dirty Checking 기반)</li>
 * </ul>
 *
 * @author 노정원
 * @since 2026-01-15
 * @see PostComment
 * @see JpaRepository
 */
public interface PostCommentRepository extends JpaRepository<PostComment, Long>{
}
