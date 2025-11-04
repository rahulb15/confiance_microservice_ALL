package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;

public class AuthenticationException extends BaseException {

    public AuthenticationException() {
        super(ResponseCode.UNAUTHORIZED);
    }

    public AuthenticationException(String message) {
        super(ResponseCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(ResponseCode responseCode) {
        super(responseCode);
    }

    public AuthenticationException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}
