package com.plog.domain.hashtag.service;


import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.post.entity.Post;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
public interface HashTagService {

    void createPostHashTag(Long postId, Long tagId, String newTagName);
     List<HashTag> getAllHashTags();
}