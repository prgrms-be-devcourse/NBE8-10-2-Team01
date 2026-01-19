package com.plog.standard.util;


import tools.jackson.databind.ObjectMapper;

/**
 * 애플리케이션 전반에서 사용되는 공통 유틸리티 모음 클래스입니다.
 * <p>
 * 내부 정적 클래스(Nested Class) 구조를 통해 기능별(JSON, 날짜, 문자열 등)로
 * 유틸리티를 네임스페이스화하여 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 유틸리티 클래스로서 상속을 고려하지 않으며, 모든 메서드는 static으로 제공됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 인스턴스화가 필요 없는 유틸리티 클래스이므로 기본 생성자의 외부 호출을 권장하지 않습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link Ut.json#objectMapper} 필드는 스프링 컨테이너 또는 초기화 설정을 통해 주입되어야 정상 작동합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Jackson Databind 라이브러리에 의존성을 가집니다.
 *
 * @author minhee
 * @see
 * @since 2026-01-15
 */

public class Ut {
    public static class json {
        public static ObjectMapper objectMapper;

        public static String toString(Object object) {
            return toString(object, null);
        }

        private static String toString(Object object, String defaultValue) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
}