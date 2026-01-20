package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author njwwn
 * @see
 * @since 2026-01-20
 */

@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode{

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    COMMENT_CREATE_FAILED(HttpStatus.NOT_FOUND, "댓글 작성에 실패하였습니다."),
    COMMENT_UPDATE_FAILED(HttpStatus.NOT_FOUND, "댓글 수정에 실패하였습니다."),
    COMMENT_DELETE_FAILED(HttpStatus.NOT_FOUND, "댓글 삭제에 실패하였습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

}
