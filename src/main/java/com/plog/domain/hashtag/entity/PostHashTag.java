package com.plog.domain.hashtag.entity;

import com.plog.domain.post.entity.Post;
import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_hashtag")
public class PostHashTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private HashTag hashTag;

    private String displayName;

    @Builder
    public PostHashTag(Post post, HashTag hashTag, String displayName) {
        this.displayName = displayName;
        this.post = post;
        this.hashTag = hashTag;
    }
}