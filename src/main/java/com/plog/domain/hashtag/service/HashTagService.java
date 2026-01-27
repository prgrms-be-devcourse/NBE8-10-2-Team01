package com.plog.domain.hashtag.service;


import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.post.entity.Post;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
public interface HashTagService {

    /**
     * 특정 게시글에 해시태그 리스트를 연결합니다.
     *
     * @param postId   연결할 게시글 ID
     * @param tagNames 해시태그 이름 리스트
     */
    void createPostHashTag(Long postId, List<String> tagNames);

}