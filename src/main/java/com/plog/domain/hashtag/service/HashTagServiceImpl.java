package com.plog.domain.hashtag.service;

import com.plog.domain.hashtag.entity.HashTag;
import com.plog.domain.hashtag.entity.PostHashTag;
import com.plog.domain.hashtag.repository.HashTagRepository;
import com.plog.domain.hashtag.repository.PostHashTagRepository;
import com.plog.domain.post.entity.Post;
import com.plog.global.exception.exceptions.HashTagException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashTagServiceImpl  implements HashTagService {

    private final HashTagRepository hashTagRepository;
    private final PostHashTagRepository postHashTagRepository;

    /**
     * 게시글 저장 시 태그를 연결하는 메서드 (Hybrid 방식)
     * @param post       저장된 게시글 객체
     * @param tagId      리스트에서 선택한 경우 (ID)
     * @param newTagName 직접 입력한 경우 (이름)
     */
    public void createPostHashTag(Post post, Long tagId, String newTagName) {
        HashTag hashTag = null;

        // 1. [선택] 사용자가 리스트에서 선택한 경우 (ID로 찾기)
        if (tagId != null) {
            hashTag = hashTagRepository.findById(tagId)
                    .orElseThrow(() -> new HashTagException("존재하지 않는 태그 ID입니다."));
        }
        // 2. [입력] 사용자가 직접 입력한 경우 (이름으로 찾거나 만들기)
        else if (newTagName != null && !newTagName.trim().isEmpty()) {
            hashTag = hashTagRepository.findByName(newTagName)
                    .orElseGet(() -> hashTagRepository.save(new HashTag(newTagName)));
        }

        // 선택도 입력도 안 했으면 저장하지 않고 종료
        if (hashTag == null) {
            return;
        }

        // 3. [저장] 연결 테이블에 저장 (DB에는 hashtag_id 숫자로 저장됨)
        postHashTagRepository.save(PostHashTag.builder()
                .post(post)
                .hashTag(hashTag)
                .build());
    }

    // 화면에 태그 목록 뿌려주기용
    @Transactional(readOnly = true)
    public List<HashTag> getAllHashTags() {
        return hashTagRepository.findAll();
    }

}
