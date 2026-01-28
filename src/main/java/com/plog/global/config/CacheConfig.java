package com.plog.global.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 애플리케이션 전반에서 사용할 캐시 설정을 담당하는 구성 클래스입니다.
 * <p>
 * 주로 JWT 리프레시 토큰(Refresh Token)과 같이 빈번하게 접근되면서도 수명이 정해진 데이터를
 * 서버 메모리에 저장하여 저장소(DB) 접근 비용을 절감하고 보안 성능을 높이는 역할을 합니다.
 *
 * <p><b>주요 설정 내용:</b><br>
 * 1. 캐시 만료 정책: {@code expireAfterWrite}를 사용하여 토큰 발급 후 설정된 시간이 지나면 자동 삭제 처리합니다. <br>
 * 2. 최대 용량 제한: 서버 메모리 보호를 위해 최대 항목 수를 제한합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link CacheManager}를 빈으로 등록하여 스프링의 추상화된 캐시 인터페이스({@code @Cacheable} 등)를 사용할 수 있게 합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Caffeine Cache (com.github.ben-manes.caffeine)
 *
 * @author minhee
 * @since 2026-01-27
 */

@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * 캐시 저장소 이름을 다른 곳에서도 사용할 수 있도록 상수로 정의합니다.
     */
    public static final String CACHE_NAME = "refreshToken";
    private final long refreshTokenExpiration;

    public CacheConfig(
            @Value("${custom.jwt.refresh-expiration}") long refreshTokenExpiration
    ) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * 스프링의 캐시 추상화를 지원하는 {@link CacheManager}를 생성합니다.
     * @return {@link CaffeineCacheManager} 인스턴스
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_NAME);
        cacheManager.setCaffeine(caffeineBuilder());
        return cacheManager;
    }

    /**
     * Caffeine 캐시의 세부 정책(만료 시간, 최대 크기 등)을 구성하는 빌더를 생성합니다.
     * @return 설정이 완료된 {@link Caffeine} 빌더 객체
     */
    private Caffeine<Object, Object> caffeineBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(refreshTokenExpiration, TimeUnit.MILLISECONDS)
                .maximumSize(1000);
    }
}