package com.plog.domain.hashtag.service;

import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.hashtag.entity.PostHashTag;
import com.plog.domain.hashtag.repository.HashTagRepository;
import com.plog.domain.hashtag.repository.PostHashTagRepository;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashTagServiceImpl implements HashTagService {

    private final HashTagRepository hashTagRepository;
    private final PostHashTagRepository postHashTagRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void createPostHashTag(Long postId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        Post post = postRepository.getReferenceById(postId);


        for (String rawName : tagNames) {

            String normalizedName = normalizeTag(rawName);


            HashTag hashTag = hashTagRepository.findByName(normalizedName)
                    .orElseGet(() -> hashTagRepository.save(new HashTag(normalizedName)));

            // 3. 중복 연결 방지 및 저장
            if (!postHashTagRepository.existsByPostIdAndHashTagId(post.getId(), hashTag.getId())) {
                PostHashTag postHashTag = PostHashTag.builder()
                        .post(post)
                        .hashTag(hashTag)
                        .displayName(rawName)
                        .build();

                postHashTagRepository.save(postHashTag);

            }
        }
    }



    private String normalizeTag(String name) {
        return name.trim().toLowerCase().replace(" ", "_");
    }
}