package com.plog.domain.post.dto;

import lombok.Builder;

/**
 * 파일을 읽어와, 사용자가 초기에 주어지는 post template seed 를 메모리에 저장할 때 사용합니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
@Builder
public record PostTemplateSeed (
       String name,
       String title,
       String content) {
}
