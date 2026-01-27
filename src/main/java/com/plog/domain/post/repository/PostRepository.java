package com.plog.domain.post.repository;

import com.plog.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Post 엔티티에 대한 데이터 액세스 기능을 제공하는 인터페이스입니다.
 * <p>
 * Spring Data JPA를 기반으로 기본적인 CRUD 기능을 수행하며,
 * 게시물 상태별 조회 및 제목/내용 키워드 검색 기능을 쿼리 메서드 형태로 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<Post, Long>}을 상속받아 표준 JPA 기능을 상속받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring Data JPA의 인프라스트럭처에 의해 런타임에 구현체가 동적으로 생성되며,
 * 스프링 컨테이너의 빈으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA 모듈을 사용합니다.
 *
 * @author MintyU
 * @since 2026-01-15
 */

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * 게시글 조회: 작성자(Member)를 한 번의 쿼리로 함께 가져옵니다.
     */
    @Query("select p from Post p join fetch p.member where p.id = :id")
    Optional<Post> findByIdWithMember(@Param("id") Long id);

    /**
     * 전체 게시글 조회: 작성자(Member)를 한 번의 쿼리로 함께 가져옵니다.
     */
    @Query("select p from Post p join fetch p.member where p.status = 'PUBLISHED'")
    Slice<Post> findAllWithMember(Pageable pageable);

    /**
     * 특정 회원 게시글 조회: memberId로 필터링하면서 작성자 정보를 함께 가져옵니다.
     */
    @Query("select p from Post p join fetch p.member where p.member.id = :memberId")
    Slice<Post> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
