package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.ErrorCode;

public class HashTagException extends BaseException{

    public HashTagException(ErrorCode errorCode) {
        super(errorCode);
    }

    public HashTagException(ErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public HashTagException(ErrorCode errorCode, String clientMessage, String logMessage) {
        super(errorCode, clientMessage, logMessage);
    }

}
