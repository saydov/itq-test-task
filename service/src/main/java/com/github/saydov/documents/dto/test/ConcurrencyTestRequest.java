package com.github.saydov.documents.dto.test;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrencyTestRequest {

    @NotNull(message = "Document ID is required")
    private Long id;

    @Builder.Default
    @Min(value = 1, message = "Threads must be at least 1")
    @Max(value = 50, message = "Threads must be at most 50")
    private int threads = 5;

    @Builder.Default
    @Min(value = 1, message = "Attempts must be at least 1")
    @Max(value = 100, message = "Attempts must be at most 100")
    private int attempts = 10;

    @NotBlank(message = "Initiator is required")
    private String initiator;
}
