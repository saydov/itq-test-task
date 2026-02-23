package com.github.saydov.documents.service.persistence;

import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentLoader {

    private final DocumentRepository documentRepository;

    public Map<Long, Document> loadMap(Collection<Long> ids) {
        return documentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Document::getId, Function.identity()));
    }
}