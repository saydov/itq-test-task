package com.github.saydov.documents.dto.request;

import com.github.saydov.documents.enums.DocumentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record DocumentSearchFilter(
        DocumentStatus status,
        String author,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
) {
}