package com.github.saydov.documents.dto.response;

import com.github.saydov.documents.enums.DocumentStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DocumentDetailResponse(
        Long id,
        String number,
        String author,
        String title,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<StatusHistoryResponse> history
) {
}
