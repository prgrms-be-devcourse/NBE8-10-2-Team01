package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가 및 유저 조회에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당 {@code AuthErrorCode} 는 {@link
 * com.plog.global.exception.exceptions.AuthException AuthException}에서 사용되며, <br>
 * {@code NAME(HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see com.plog.global.exception.exceptions.AuthException AuthException
 * @since 2026-01-15
 */
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "can't get user"),
    USER_CREATE_FAIL(HttpStatus.BAD_REQUEST, "failed to createWithCategory user"),
    USER_UPDATE_FAIL(HttpStatus.BAD_REQUEST, "failed to update user"),
    USER_DELETE_FAIL(HttpStatus.BAD_REQUEST, "failed to delete user"),
    SIGNUP_FAIL(HttpStatus.BAD_REQUEST, "failed to sign up"),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "failed to login"),
    USER_AUTH_FAIL(HttpStatus.FORBIDDEN, "user authorization failed"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "token has expired"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "invalid token"),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "login required");

    private final HttpStatus status;
    private final String message;


    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}