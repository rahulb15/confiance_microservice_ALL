package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final ResponseCode responseCode;
    private final Object data;

    protected BaseException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.data = null;
    }

    protected BaseException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
        this.data = null;
    }

    protected BaseException(ResponseCode responseCode, String message, Object data) {
        super(message);
        this.responseCode = responseCode;
        this.data = data;
    }

    protected BaseException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.data = null;
    }
}