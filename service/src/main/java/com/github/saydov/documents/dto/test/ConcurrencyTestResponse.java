package com.github.saydov.documents.dto.test;

import com.github.saydov.documents.enums.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConcurrencyTestResponse {

    private final Long id;

    private final int totalAttempts;
    private final int successCount, conflictCount, errorCount;

    private final DocumentStatus finalStatus;
}
