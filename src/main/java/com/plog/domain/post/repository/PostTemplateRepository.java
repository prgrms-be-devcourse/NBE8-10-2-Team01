package com.plog.domain.post.repository;

import com.plog.domain.post.entity.PostTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PostTemplate 에 대한 DAO 인터페이스입니다.
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<{PostTemplate}, Long>}을 상속받아 표준 JPA 기능을 상속받습니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
public interface PostTemplateRepository extends JpaRepository<PostTemplate, Long> {

    List<PostTemplate> findAllByMember_Id(Long memberId);
}
