package com.github.saydov.documents.dto;

import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.enums.DocumentStatus;

public record StatusTransition(DocumentStatus targetStatus, String initiator, String comment) {

    public boolean isApproval() {
        return targetStatus == DocumentStatus.APPROVED;
    }

    private static StatusTransition buildWithStatus(BatchStatusRequest request, DocumentStatus status) {
        return new StatusTransition(status, request.initiator(), request.comment());
    }

    public static StatusTransition submit(BatchStatusRequest request) {
        return buildWithStatus(request, DocumentStatus.SUBMITTED);
    }

    public static StatusTransition approve(BatchStatusRequest request) {
        return buildWithStatus(request, DocumentStatus.APPROVED);
    }
}
