package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;

public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(ResponseCode.VALIDATION_FAILED, message);
    }

    public ValidationException(String message, Object data) {
        super(ResponseCode.VALIDATION_FAILED, message, data);
    }
}