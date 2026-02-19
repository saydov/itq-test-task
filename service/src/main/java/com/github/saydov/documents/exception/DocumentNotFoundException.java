package com.github.saydov.documents.exception;

import org.springframework.http.HttpStatus;

public class DocumentNotFoundException extends DocumentServiceException {

    public DocumentNotFoundException(Long id) {
                super(HttpStatus.NOT_FOUND, "Document not found: " + id);
    }
}
