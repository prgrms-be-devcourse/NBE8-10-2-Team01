package com.plog.domain.image.repository;

import com.plog.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 이미지 엔티티의 영속성을 관리하는 저장소 인터페이스입니다.
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속받아
 * 기본적인 CRUD(Create, Read, Update, Delete) 메서드를 자동으로 제공합니다.
 * 별도의 구현체 없이도 스프링이 런타임에 프록시 객체를 생성하여 DB 접근 로직을 처리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link JpaRepository}를 상속받아 페이징, 정렬, CRUD 기능을 모두 포함합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Repository} 어노테이션이 적용되어 스프링 빈으로 자동 등록되며,
 * JPA 예외를 스프링의 DataAccessException 계층 예외로 변환하는 기능을 수행합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA 라이브러리를 통해 Hibernate와 같은 ORM 구현체와 상호작용합니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-20
 */

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

}
