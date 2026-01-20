package com.plog.testUtil;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 추후 spring security 가 작성되면 테스트 중 유저 정보를 가져올 때 사용하는 어노테이션
 *
 * @author jack8
 * @since 2026-01-20
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithSecurityMockUserContextFactory.class)
public @interface WithCustomMockUser {

    long userId() default 1L;

}
