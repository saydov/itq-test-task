package com.github.saydov.documents.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ApprovalSearchFilter(
        String approvedBy,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
) {
}