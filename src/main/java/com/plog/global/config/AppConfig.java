package com.plog.global.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 전역에서 사용되는 공통 인프라 빈을 설정하는 클래스입니다.
 * <p>
 * <b>설계 의도:</b><br>
 * 특정 도메인에 종속되지 않는 범용 빈을 중앙 관리합니다.<br>
 * 설정 클래스 간의 복잡한 의존성 관계를 끊기 위해 공통 컴포넌트를 독립적인 설정 계층으로 분리합니다.<br>
 * 향후 별도의 공통 Util 빈 등이 추가될 수 있습니다.
 *
 * <p><b>빈 관리:</b><br>
 * - {@link ObjectMapper}: JSON 데이터 파싱 및 Java 객체 매핑 담당
 *
 * @author minhee
 * @see com.plog.global.security.SecurityConfig
 * @since 2026-01-22
 */

@Configuration
public class AppConfig {

    /**
     * 전역적으로 사용할 ObjectMapper 설정입니다.
     * <p>
     * <b>주요 사용처:</b><br>
     * - SecurityConfig 내 예외 핸들러의 JSON 응답 생성
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}