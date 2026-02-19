package com.github.saydov.documents.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DocumentServiceException extends RuntimeException {

    private final HttpStatus statusCode;

    protected DocumentServiceException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
