package com.github.saydov.documents.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApprovalRegistryResponse(
        Long id,
        Long documentId,
        String documentNumber,
        String documentTitle,
        String approvedBy,
        LocalDateTime approvedAt
) {
}