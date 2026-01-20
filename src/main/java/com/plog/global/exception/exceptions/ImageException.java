package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.ErrorCode;

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