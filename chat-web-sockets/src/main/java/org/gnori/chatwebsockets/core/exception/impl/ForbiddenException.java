package org.gnori.chatwebsockets.core.exception.impl;

import org.gnori.chatwebsockets.core.exception.BusinessLogicException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessLogicException {

    private static final String DEFAULT_MESSAGE_EXCEPTION = "You don't have enough rights to do this";

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException() {
        super(DEFAULT_MESSAGE_EXCEPTION, HttpStatus.FORBIDDEN);
    }
}
