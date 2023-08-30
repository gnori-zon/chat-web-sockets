package org.gnori.chatwebsockets.core.exception.impl;

import org.gnori.chatwebsockets.core.exception.BusinessLogicException;
import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessLogicException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
