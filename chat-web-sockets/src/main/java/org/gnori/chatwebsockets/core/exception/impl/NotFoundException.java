package org.gnori.chatwebsockets.core.exception.impl;

import org.gnori.chatwebsockets.core.exception.BusinessLogicException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessLogicException {

    private static final String DEFAULT_MESSAGE_EXCEPTION = "Oops, not found";

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException() {
        super(DEFAULT_MESSAGE_EXCEPTION, HttpStatus.NOT_FOUND);
    }
}
