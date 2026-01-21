package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.ErrorCode;

/**
 * 이미지 처리 도메인에서 발생하는 모든 예외를 담당하는 커스텀 예외 클래스입니다.
 * <p>
 * 파일 업로드 실패, 지원하지 않는 확장자, 용량 초과 등 이미지와 관련된
 * 비즈니스 로직 처리 중 발생하는 예외 상황을 캡슐화합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException}을 상속받아 전역 예외 처리기(GlobalExceptionHandler)에서 일관된 포맷으로 처리됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageException(ErrorCode errorCode)} <br>
 * 기본 에러 메시지만 사용하는 단순 예외를 생성합니다. <br>
 *
 * {@code ImageException(ErrorCode errorCode, String logMessage)} <br>
 * 서버 내부 로그용 상세 메시지를 포함하여 예외를 생성합니다. (디버깅 용도) <br>
 *
 * {@code ImageException(ErrorCode errorCode, String logMessage, String clientMessage)} <br>
 * 클라이언트에게 보여줄 메시지를 별도로 커스터마이징해야 할 때 사용합니다. <br>
 *
 * @author Jaewon Ryu
 * @see BaseException
 * @since 2026-01-21
 */

public class ImageException extends BaseException {

    public ImageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public ImageException(ErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}