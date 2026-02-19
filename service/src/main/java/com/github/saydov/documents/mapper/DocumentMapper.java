package com.github.saydov.documents.mapper;

import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.dto.response.DocumentResponse;
import com.github.saydov.documents.dto.response.StatusHistoryResponse;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.entity.StatusHistory;
import org.springframework.stereotype.Component;

@Component
public final class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public DocumentDetailResponse toDetailResponse(Document document) {
        return DocumentDetailResponse.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .history(document.getStatusHistory().stream().map(this::toHistoryResponse).toList())
                .build();
    }

    public StatusHistoryResponse toHistoryResponse(StatusHistory history) {
        return StatusHistoryResponse.builder()
                .id(history.getId())
                .action(history.getAction())
                .initiator(history.getInitiator())
                .comment(history.getComment())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
