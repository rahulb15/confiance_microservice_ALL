package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;

public class AuthorizationException extends BaseException {

    public AuthorizationException() {
        super(ResponseCode.FORBIDDEN);
    }

    public AuthorizationException(String message) {
        super(ResponseCode.FORBIDDEN, message);
    }

    public AuthorizationException(ResponseCode responseCode) {
        super(responseCode);
    }

    public AuthorizationException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}