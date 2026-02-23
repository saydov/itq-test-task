package com.github.saydov.documents.exception;

import org.springframework.http.HttpStatus;

public class DocumentNotFoundException extends DocumentServiceException {

    public DocumentNotFoundException(Long documentId) {
        super(HttpStatus.NOT_FOUND, "Document not found: " + documentId);
    }
}
