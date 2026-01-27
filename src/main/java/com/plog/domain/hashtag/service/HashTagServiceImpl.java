package com.plog.domain.hashtag.service;

import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.hashtag.entity.PostHashTag;
import com.plog.domain.hashtag.repository.HashTagRepository;
import com.plog.domain.hashtag.repository.PostHashTagRepository;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.exceptions.HashTagException;
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
    public void createPostHashTag(Long postId, Long tagId, String newTagName) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        HashTag hashTag = null;
        String displayName = "";


        if (tagId != null) {
            hashTag = hashTagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID입니다."));
            displayName = hashTag.getName();
        }

        else if (newTagName != null && !newTagName.trim().isEmpty()) {
            String rawTag = newTagName.trim();
            String normalizedName = normalizeTag(rawTag);


            hashTag = hashTagRepository.findByName(normalizedName)
                    .orElseGet(() -> hashTagRepository.save(new HashTag(normalizedName)));

            displayName = rawTag;
        }

        if (hashTag == null) return;

        // 4. [저장] 중복 체크 후 저장
        if (!postHashTagRepository.existsByPostIdAndHashTagId(post.getId(), hashTag.getId())) {

            PostHashTag postHashTag = PostHashTag.builder()
                    .post(post)
                    .hashTag(hashTag)
                    .displayName(displayName)
                    .build();

            // DB 저장
            postHashTagRepository.save(postHashTag);

            // 양방향 연관관계 설정
            //post.addPostHashTag(postHashTag);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<HashTag> getAllHashTags() {
        return hashTagRepository.findAll();
    }

    private String normalizeTag(String keyword) {
        return keyword.trim()
                .replaceAll("\\s+", " ")
                .replace(" ", "_")
                .toLowerCase();
    }
}