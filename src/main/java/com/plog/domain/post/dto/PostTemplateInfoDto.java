package com.plog.domain.post.dto;

import com.plog.domain.post.entity.PostTemplate;
import lombok.Builder;

/**
 * post template 을 생성하기 위한 record 입니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
@Builder
public record PostTemplateInfoDto(

        Long id,

        String name,

        String title,

        String content
) {

    public static PostTemplateInfoDto to(PostTemplate template) {
        return PostTemplateInfoDto.builder()
                .id(template.getId())
                .name(template.getName())
                .title(template.getTitle())
                .content(template.getContent())
                .build();
    }
}
