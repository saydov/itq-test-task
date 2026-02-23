package com.github.saydov.documents.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DocumentServiceException extends RuntimeException {

    private final HttpStatus status;

    protected DocumentServiceException(HttpStatus httpStatus, String message) {
        super(message);
        this.status = httpStatus;
    }
}
