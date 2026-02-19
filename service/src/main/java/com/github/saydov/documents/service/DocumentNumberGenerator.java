package com.github.saydov.documents.service;

import com.github.saydov.documents.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DocumentNumberGenerator {

    private static final String NUMBER_FORMAT = "%s-%06d";

    private final DocumentRepository documentRepository;

    @Value("${app.document.number-prefix:DOC}")
    private String prefix;

    public String next() {
        return NUMBER_FORMAT.formatted(prefix, documentRepository.nextDocumentNumber());
    }
}
