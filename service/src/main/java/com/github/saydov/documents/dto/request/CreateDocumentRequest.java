package com.github.saydov.documents.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateDocumentRequest(
        @NotBlank(message = "Author is required") String author,
        @NotBlank(message = "Title is required") String title
) {
}
