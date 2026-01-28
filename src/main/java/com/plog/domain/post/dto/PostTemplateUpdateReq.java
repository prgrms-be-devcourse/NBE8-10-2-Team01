package com.plog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * postTemplate 을 갱신할 때 사용하는 dto 입니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
public record PostTemplateUpdateReq(
        @NotBlank
        String name,
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
