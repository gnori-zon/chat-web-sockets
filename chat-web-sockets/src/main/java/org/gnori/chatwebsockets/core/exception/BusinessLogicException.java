package org.gnori.chatwebsockets.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessLogicException extends RuntimeException {

    private final HttpStatus status;

    public BusinessLogicException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
