package com.github.saydov.documents.exception;

import org.springframework.http.HttpStatus;

public class ApprovalRegistryException extends DocumentServiceException {

    public ApprovalRegistryException(Long documentId) {
        super(HttpStatus.CONFLICT, "Document already approved: " + documentId);
    }
}
