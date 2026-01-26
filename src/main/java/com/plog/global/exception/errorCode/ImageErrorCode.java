package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 이미지 처리 과정에서 발생할 수 있는 구체적인 에러 상황들을 정의한 Enum 클래스입니다.
 * <p>
 * {@link ErrorCode} 인터페이스를 구현하여, 각 에러 상황에 맞는 HTTP 상태 코드와
 * 클라이언트에게 전달할 기본 메시지를 매핑하고 있습니다.
 * {@link com.plog.global.exception.exceptions.ImageException} 발생 시 인자로 사용됩니다.
 *
 * <p><b>구성 요소:</b><br>
 * - <b>Server Error (5xx):</b> MinIO 저장소 연동 실패, 파일 I/O 오류 등 시스템 문제 <br>
 * - <b>Client Error (4xx):</b> 파일 누락, 지원하지 않는 확장자 등 잘못된 요청
 *
 * <p><b>주요 패턴:</b><br>
 * {@code @AllArgsConstructor}와 {@code @Getter}를 사용하여 불변 필드를 관리합니다.
 *
 * @author Jaewon Ryu
 * @see com.plog.global.exception.exceptions.ImageException
 * @since 2026-01-21
 */

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    
    // 500: 서버 내부 에러 (MinIO 연결 실패 등)
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),
    BUCKET_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장소 초기화에 실패했습니다."),

    // 400: 클라이언트 에러
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "파일이 비어있습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}