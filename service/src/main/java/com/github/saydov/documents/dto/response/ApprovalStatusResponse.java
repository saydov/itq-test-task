package com.github.saydov.documents.dto.response;

import lombok.Builder;

@Builder
public record ApprovalStatusResponse(
        Long documentId,
        boolean approved
) {
}
