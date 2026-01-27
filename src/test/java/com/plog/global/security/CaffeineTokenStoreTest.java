package com.plog.global.security;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.concurrent.TimeUnit;

import static com.plog.global.config.CacheConfig.CACHE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CaffeineTokenStore}의 핵심 기능을 검증하기 위한 단위 테스트 클래스입니다.
 * <p>
 * 실제 스프링 컨텍스트를 로드하지 않고, Caffeine Cache 객체를 직접 구성하여
 * 로컬 메모리 상에서의 데이터 저장, 조회, 삭제 정합성을 빠르게 검증합니다.
 *
 * <p><b>작동 원리:</b><br>
 * {@link CaffeineCacheManager}를 수동으로 생성하고 테스트 대상인 {@code TokenStore}에 주입하여
 * 추상화된 캐시 인터페이스가 실제 Caffeine 라이브러리와 올바르게 상호작용하는지 확인합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * - Caffeine Cache: 인메모리 캐시 엔진 <br>
 * - Spring Cache: 캐시 추상화 레이어
 *
 * @author minhee
 * @since 2026-01-27
 */

class CaffeineTokenStoreTest {

    private CaffeineTokenStore tokenStore;
    private CaffeineCacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 실제 Caffeine 설정과 유사하게 CacheManager를 수동 생성합니다.
        cacheManager = new CaffeineCacheManager(CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .maximumSize(1000));

        tokenStore = new CaffeineTokenStore(cacheManager);
    }

    @Test
    @DisplayName("토큰 저장 및 조회 성공")
    void saveAndGetToken_Success() {
        // given
        String email = "test@plog.com";
        String refreshToken = "sample-refresh-token";

        // when
        tokenStore.save(email, refreshToken);
        String savedToken = tokenStore.get(email);

        // then
        assertThat(savedToken).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("기존 토큰이 존재할 때 새로운 토큰을 저장하면 값이 갱신된다")
    void updateToken_Success() {
        // given
        String email = "test@plog.com";
        String oldToken = "old-token";
        String newToken = "new-token";
        tokenStore.save(email, oldToken);

        // when
        tokenStore.save(email, newToken);
        String savedToken = tokenStore.get(email);

        // then
        assertThat(savedToken).isEqualTo(newToken);
        assertThat(savedToken).isNotEqualTo(oldToken);
    }

    @Test
    @DisplayName("토큰 삭제 후 조회하면 null을 반환한다")
    void deleteToken_Success() {
        // given
        String email = "test@plog.com";
        tokenStore.save(email, "some-token");

        // when
        tokenStore.delete(email);
        String savedToken = tokenStore.get(email);

        // then
        assertThat(savedToken).isNull();
    }

    @Test
    @DisplayName("저장되지 않은 이메일로 조회하면 null을 반환한다")
    void getNonExistentToken_ReturnsNull() {
        // when
        String savedToken = tokenStore.get("unknown@plog.com");

        // then
        assertThat(savedToken).isNull();
    }
}