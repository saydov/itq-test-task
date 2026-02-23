package com.github.saydov.documents.service;

import com.github.saydov.documents.dto.request.CreateDocumentRequest;
import com.github.saydov.documents.dto.request.DocumentSearchFilter;
import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.exception.DocumentNotFoundException;
import com.github.saydov.documents.mapper.DocumentMapper;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.repository.domain.DocumentSpecifications;
import com.github.saydov.documents.service.persistence.DocumentNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentNumberGenerator numberGenerator;
    private final DocumentMapper mapper;

    @Transactional
    public DocumentDetailResponse create(CreateDocumentRequest request) {
        var document = Document.of(numberGenerator.next(), request.author(), request.title());
        var saved = documentRepository.save(document);
        log.info("Document created: id={}, number={}", saved.getId(), saved.getNumber());
        return mapper.buildDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getById(Long id) {
        return documentRepository.findById(id)
                .map(mapper::buildDetailResponse)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentDetailResponse> getByIds(List<Long> ids) {
        return documentRepository.findByIdIn(ids)
                .map(mapper::buildDetailResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DocumentDetailResponse> search(DocumentSearchFilter filter, Pageable pageable) {
        return documentRepository.findAll(DocumentSpecifications.fromFilter(filter), pageable)
                .map(mapper::buildDetailResponse);
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByStatus(DocumentStatus status, int limit) {
        return documentRepository.findTopByStatus(status, Pageable.ofSize(limit))
                .stream()
                .map(Document::getId)
                .toList();
    }
}
