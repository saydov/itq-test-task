package com.github.saydov.documents.dto.response;

import com.github.saydov.documents.enums.DocumentAction;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StatusHistoryResponse(
        Long id,
        DocumentAction action,
        String initiator,
        String comment,
        LocalDateTime createdAt
) {
}
