package com.plog.global.response;

/**
 * 응답 객체에 대한 정의 인터페이스입니다.
 *
 * <p>getStatus, getMessage 에 대한 정의를 강제합니다.
 *
 *
 * @author jack8
 * @since 2026-01-15
 */
public interface Response<T> {

    String getStatus();

    T getData();

    String getMessage();
}
