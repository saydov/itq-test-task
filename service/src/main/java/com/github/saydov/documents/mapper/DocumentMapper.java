package com.github.saydov.documents.mapper;

import com.github.saydov.documents.dto.response.ApprovalRegistryResponse;
import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.dto.response.StatusHistoryResponse;
import com.github.saydov.documents.entity.ApprovalRegistry;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.entity.StatusHistory;
import org.springframework.stereotype.Component;

@Component
public final class DocumentMapper {

    public DocumentDetailResponse buildDetailResponse(Document document) {
        var history = document.getStatusHistory().stream()
                .map(this::buildHistoryResponse)
                .toList();

        return DocumentDetailResponse.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .history(history)
                .build();
    }

    public StatusHistoryResponse buildHistoryResponse(StatusHistory history) {
        return StatusHistoryResponse.builder()
                .id(history.getId())
                .action(history.getAction())
                .initiator(history.getInitiator())
                .comment(history.getComment())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public ApprovalRegistryResponse buildApprovalResponse(ApprovalRegistry registry) {
        var document = registry.getDocument();
        return ApprovalRegistryResponse.builder()
                .id(registry.getId())
                .documentId(document.getId())
                .documentNumber(document.getNumber())
                .documentTitle(document.getTitle())
                .approvedBy(registry.getApprovedBy())
                .approvedAt(registry.getApprovedAt())
                .build();
    }
}
