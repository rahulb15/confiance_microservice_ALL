package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;

public class BusinessException extends BaseException {

    public BusinessException(ResponseCode responseCode) {
        super(responseCode);
    }

    public BusinessException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    public BusinessException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }
}
