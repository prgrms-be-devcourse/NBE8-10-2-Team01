package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.ErrorCode;
import com.plog.global.exception.errorCode.HashTagErrorCode;

public class HashTagException extends BaseException{

    public HashTagException(HashTagErrorCode errorCode) {
        super(errorCode);
    }

    public HashTagException(HashTagErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public HashTagException(HashTagErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }

}
