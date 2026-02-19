package com.github.saydov.documents.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BatchStatusRequest(
        @NotEmpty(message = "Document IDs are required")
        @Size(min = 1, max = 1000, message = "Number of IDs must be between 1 and 1000")
        List<Long> ids,

        @NotBlank(message = "Initiator is required")
        String initiator,

        String comment
) {
}
