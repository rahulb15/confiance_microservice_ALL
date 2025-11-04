package com.microservices.common.core.exception;

import com.microservices.common.core.enums.ResponseCode;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resource) {
        super(ResponseCode.NOT_FOUND, resource + " not found");
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(ResponseCode.NOT_FOUND, resource + " with identifier '" + identifier + "' not found");
    }
}