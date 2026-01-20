package com.plog.global.exception.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    
    // 500: 서버 내부 에러 (MinIO 연결 실패 등)
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),
    BUCKET_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장소 초기화에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}