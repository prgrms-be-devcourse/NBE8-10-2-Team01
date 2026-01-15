package com.plog.global.exception.exceptionHadler;

/**
 * exception handler 의 처리 순서에 대한 상수 값을 정의하고 관리하는 객체입니다.
 *
 * <p>public 한 static final 내부 필드를 통해 값을 관리합니다.
 *
 * @author jack8
 * @since 2026-01-15
 */
public class ExceptionHandlerOrder {
    public static final int GLOBAL_EXCEPTION_HANDLER = 1;

    public static final int DEFAULT_EXCEPTION_HANDLER = 2;
}

