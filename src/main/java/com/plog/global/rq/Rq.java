package com.plog.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;
import java.util.Optional;

/**
 * 현재 HTTP 요청과 관련된 상태를 유지하고, 공통적으로 사용되는 기능을 제공하는 유틸리티 클래스
 *
 * <p><b>작동 원리:</b><br>
 * HTTP 요청마다 독립적인 인스턴스가 생성되어, 컨트롤러나 서비스 계층에서 HttpServletRequest/Response에
 * 직접 접근하지 않고도 헤더, 쿠키 관리 등의 작업을 간편하게 수행할 수 있도록 돕습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code Rq(HttpServletRequest req, HttpServletResponse res)}  <br>
 * Lombok의 {@code @RequiredArgsConstructor}를 통해 서블릿 객체들을 주입받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RequestScope}를 통해 매 HTTP 요청마다 새로운 빈 인스턴스가 생성되고 요청 종료 시 소멸됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author minhee
 * @since 2026-01-20
 */

@RequestScope
@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse res;

    /**
     * 특정 HTTP 헤더 값을 가져옵니다. 값이 없거나 비어있으면 기본값을 반환합니다.
     * @param name 헤더 이름
     * @param defaultValue 기본값
     * @return 헤더 값 또는 기본값
     */
    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    /**
     * HTTP 응답 헤더를 설정합니다.
     * @param name 헤더 이름
     * @param value 설정할 값 (null 혹은 빈 문자열일 경우 속성 제거 시도)
     */
    public void setHeader(String name, String value) {
        if (value == null) value = "";

        if (value.isBlank()) {
            req.removeAttribute(name);
        } else {
            res.setHeader(name, value);
        }
    }

    /**
     * 쿠키 이름을 통해 특정 쿠키의 값을 가져옵니다.
     * @param name 쿠키 이름
     * @param defaultValue 쿠키가 없을 때 반환할 기본값
     * @return 쿠키 값 또는 기본값
     */
    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(val -> val != null && !val.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    /**
     * 새로운 쿠키를 생성하여 응답에 추가합니다.
     * 기본 설정: Path="/", HttpOnly=true
     * @param name 쿠키 이름
     * @param value 쿠키 값 (빈 문자열일 경우 즉시 만료되도록 설정)
     */
    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        if (value.isBlank()) cookie.setMaxAge(0);

        res.addCookie(cookie);
    }

    /**
     * 특정 이름의 쿠키를 삭제합니다.
     * @param name 삭제할 쿠키 이름
     */
    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}