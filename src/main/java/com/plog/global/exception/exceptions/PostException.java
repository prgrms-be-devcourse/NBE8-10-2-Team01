package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.PostErrorCode;

/**
 * 게시글 서비스 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 * <p>
 * {@link PostErrorCode}를 포함하여 발생한 예외의 성격과 내부 로그 메시지를 관리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException}을 상속받는 구체 예외 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostException(PostErrorCode errorCode)} <br>
 * 에러 코드를 통해 기본적인 예외를 생성합니다. <br>
 * {@code PostException(PostErrorCode errorCode, String logMessage)} <br>
 * 내부 추적을 위한 로그 메시지를 포함하여 예외를 생성합니다.
 *
 * @author MintyU
 * @see BaseException
 * @see PostErrorCode
 * @since 2026-01-16
 */
public class PostException extends BaseException {

    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }

    public PostException(PostErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public PostException(PostErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}