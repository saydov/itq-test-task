package com.github.saydov.documents.service.persistence;

import com.github.saydov.documents.configuration.AppProperties;
import com.github.saydov.documents.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DocumentNumberGenerator {

    private static final String NUMBER_FORMAT = "%s-%06d";

    private final DocumentRepository documentRepository;
    private final AppProperties properties;

    public String next() {
        return NUMBER_FORMAT.formatted(properties.getDocument().numberPrefix(), documentRepository.nextDocumentNumber());
    }
}
