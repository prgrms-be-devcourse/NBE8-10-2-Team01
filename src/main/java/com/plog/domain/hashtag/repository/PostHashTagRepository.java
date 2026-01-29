package com.plog.domain.hashtag.repository;

import com.plog.domain.hashtag.entity.PostHashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostHashTagRepository extends JpaRepository<PostHashTag, Long> {
    // 게시글 상세 조회 시 태그 이름들만 가져오기
    @Query("SELECT h.name FROM PostHashTag ph JOIN ph.hashTag h WHERE ph.post.id = :postId")
    List<String> findHashTagNamesByPostId(@Param("postId") Long postId);

    boolean existsByPostIdAndHashTagId(Long postId, Long hashTagId);

    @Modifying
    @Query("delete from PostHashTag p where p.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}