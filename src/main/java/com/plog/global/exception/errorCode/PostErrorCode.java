package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 게시글 관련 로직에서 발생하는 예외에 대한 상수 값을 정의합니다.
 * <p>
 * {@link com.plog.global.exception.exceptions.PostException PostException}에서 사용되며,
 * 상황에 맞는 HTTP 상태 코드와 메시지를 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author MintyU
 * @see ErrorCode
 * @see com.plog.global.exception.exceptions.PostException
 * @since 2026-01-16
 */
@AllArgsConstructor
public enum PostErrorCode implements ErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_CREATE_FAIL(HttpStatus.BAD_REQUEST, "게시글 작성에 실패했습니다."),
    POST_UPDATE_FAIL(HttpStatus.BAD_REQUEST, "게시글 수정에 실패했습니다."),
    POST_DELETE_FAIL(HttpStatus.BAD_REQUEST, "게시글 삭제에 실패했습니다.");

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