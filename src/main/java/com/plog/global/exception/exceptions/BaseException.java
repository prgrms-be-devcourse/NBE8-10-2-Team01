package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.ErrorCode;
import lombok.Getter;

/**
 * 모든 커스텀 예외 클래스의 추상 부모 클래스입니다. 자체적으로는 사용되지 않습니다.
 *
 * <p>클라이언트로의 응답 코드, 상태 메시지, 내부 로그 메시지를 분리하여 저장합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code RuntimeException} 을 상속받으며 언체크 예외입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code BaseException(ErrorCode errorCode)} <br>
 * 내부 로그 메시지를 담지 않는 예외를 생성합니다. 단, protected로 점겨 있어 상속 객체만 사용이 가능합니다. <br>
 * {@code BaseException(ErrorCode errorCode, String logMessage)} <br>
 * 내부 로그 메시지를 담는 예외를 생성합니다. 단, protected로 점겨 있어 상속 객체만 사용이 가능합니다. <br>
 *
 * @author jack8
 * @see RuntimeException
 * @see ErrorCode
 * @since 2026-01-15
 */
@Getter
public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String logMessage;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage(), null, false, false);
        this.errorCode = errorCode;
        this.logMessage = "[client] " + errorCode.getMessage();
    }

    protected BaseException(ErrorCode errorCode, String logMessage) {
        super(errorCode.getMessage(), null, false, false);
        this.errorCode = errorCode;
        this.logMessage = "[internal] " + logMessage;
    }

    protected BaseException(ErrorCode errorCode, String logMessage, String clientMessage) {
        super(clientMessage, null, false, false);
        this.errorCode = errorCode;
        this.logMessage = "[internal] " + logMessage;
    }
}
