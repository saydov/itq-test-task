package com.github.saydov.documents.exception;

import org.springframework.http.HttpStatus;

public class ApprovalNotFoundException extends DocumentServiceException {

    public ApprovalNotFoundException(Long documentId) {
        super(HttpStatus.NOT_FOUND, "Approval not found for document: " + documentId);
    }
}
