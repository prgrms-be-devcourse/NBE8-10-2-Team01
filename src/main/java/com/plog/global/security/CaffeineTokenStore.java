package com.plog.global.security;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import static com.plog.global.config.CacheConfig.CACHE_NAME;


/**
 * {@link TokenStore}의 Caffeine Cache 기반 구현체입니다.
 * <p>
 * {@link CacheManager}를 통해 관리되는 로컬 메모리 캐시에
 * 토큰 정보를 저장하며, 빠른 액세스 속도와 설정된 만료 정책에 따른 자동 관리를 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link TokenStore} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code CaffeineTokenStore(CacheManager cacheManager)} <br>
 * 스프링 컨테이너로부터 설정된 CacheManager를 주입받아 초기화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component}로 등록되어 있으며, 인증 서비스 레이어에서 주입받아 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Cache Abstraction을 사용합니다.
 *
 * @author minhee
 * @see com.plog.global.config.CacheConfig
 * @since 2026-01-27
 */

@Component
@RequiredArgsConstructor
public class CaffeineTokenStore implements TokenStore {
    private final CacheManager cacheManager;

    @Override
    public void save(String email, String refreshToken) {
        Cache refreshTokenCache = cacheManager.getCache(CACHE_NAME);
        if (refreshTokenCache != null) {
            refreshTokenCache.put(email, refreshToken);
        }
    }

    @Override
    public String get(String email) {
        Cache refreshTokenCache = cacheManager.getCache(CACHE_NAME);
        if (refreshTokenCache != null) {
            return refreshTokenCache.get(email, String.class);
        }
        return null;
    }

    @Override
    public void delete(String email) {
        Cache refreshTokenCache = cacheManager.getCache(CACHE_NAME);
        if (refreshTokenCache != null) {
            refreshTokenCache.evict(email);
        }
    }
}