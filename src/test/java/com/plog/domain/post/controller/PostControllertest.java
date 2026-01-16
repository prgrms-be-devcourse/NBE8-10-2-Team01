package com.plog.domain.post.controller;

/**
 * PostController의 웹 계층 동작을 검증하는 테스트 클래스입니다.
 * <p>
 * MockMvc를 사용하여 HTTP 요청과 응답을 시뮬레이션하며,
 * 서비스 계층을 Mocking하여 컨트롤러의 로직만을 독립적으로 테스트합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 스프링 테스트 프레임워크에 의해 MockMvc와 필요한 빈들이 자동으로 주입됩니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @WebMvcTest}를 통해 PostController 관련 빈들만 로드하여 테스트 환경을 구성합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * MockMvc, Mockito, Jackson(ObjectMapper) 등을 사용합니다.
 *
 * @author MintyU
 * @since 2026-01-16
 */

public class PostControllertest {
}
