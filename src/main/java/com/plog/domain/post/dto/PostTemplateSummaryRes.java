package com.plog.domain.post.dto;

import com.plog.domain.post.entity.PostTemplate;
import lombok.Builder;

/**
 * 사용자가 본인이 작성한 템플릿을 가져올 때 리스트로서 반환되는 dto 입니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
@Builder
public record PostTemplateSummaryRes(
        String name,
        Long id
) {
    public static PostTemplateSummaryRes to(PostTemplate postTemplate) {
        return PostTemplateSummaryRes.builder()
                .name(postTemplate.getName())
                .id(postTemplate.getId())
                .build();
    }
}
