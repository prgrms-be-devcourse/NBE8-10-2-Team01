package com.plog.domain.hashtag.entity;

import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 데이터의 중복을 막고, 효율적으로 관리하기 위해서 해시태그 이름에 유니크 제약조건을 추가합니다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HashTag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Builder
    public HashTag(String name) {
        this.name = name;
    }
}