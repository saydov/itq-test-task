package com.github.saydov.documents.exception;

import org.springframework.http.HttpStatus;

public class ApprovalRegistryException extends DocumentServiceException {

    public ApprovalRegistryException(Long documentId) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create approval registry entry for document " + documentId);
    }
}
