package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum HashTagErrorCode implements ErrorCode {
    HASHTAG_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 게시글을 찾을 수 없습니다."),
    HASHTAG_CREATE_FAIL(HttpStatus.BAD_REQUEST, "failed to create hashtag");



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
