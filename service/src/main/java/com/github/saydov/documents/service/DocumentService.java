package com.github.saydov.documents.service;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.dto.request.CreateDocumentRequest;
import com.github.saydov.documents.dto.request.DocumentSearchFilter;
import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.dto.response.DocumentResponse;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.exception.ApprovalRegistryException;
import com.github.saydov.documents.exception.DocumentNotFoundException;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.mapper.DocumentMapper;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.repository.domain.DocumentSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStateService stateService;
    private final DocumentNumberGenerator numberGenerator;
    private final DocumentMapper mapper;

    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        var entity = documentRepository.save(Document.of(numberGenerator.next(), request.author(),
                request.title(), DocumentStatus.DRAFT));
        log.info("Created entity id={} number={}", entity.getId(), entity.getNumber());
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocument(Long id) {
        var entity = documentRepository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));

        return mapper.toDetailResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(List<Long> ids) {
        return documentRepository.findByIdIn(ids).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(DocumentSearchFilter filter, Pageable pageable) {
        return documentRepository.findAll(DocumentSpecifications.fromFilter(filter), pageable)
                .map(mapper::toResponse);
    }

    public List<StatusChangeResult> submitDocuments(BatchStatusRequest request) {
        return executeForEach(request.ids(),
                id -> stateService.submitSingle(id, request.initiator(), request.comment()));
    }

    public List<StatusChangeResult> approveDocuments(BatchStatusRequest request) {
        return executeForEach(request.ids(),
                id -> stateService.approveSingle(id, request.initiator(), request.comment()));
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByStatus(DocumentStatus status, int limit) {
        return documentRepository.findTopByStatus(status, Pageable.ofSize(limit))
                .stream()
                .map(Document::getId)
                .toList();
    }

    private List<StatusChangeResult> executeForEach(List<Long> ids, Function<Long, StatusChangeResult> action) {
        var results = new ArrayList<StatusChangeResult>();
        for (var id : ids) {
            try {
                results.add(action.apply(id));
            } catch (ApprovalRegistryException e) {
                log.error("Registry error for document id={}: {}", id, e.getMessage());
                results.add(StatusChangeResult.registryError(id, e.getMessage()));
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict for document id={}", id);
                results.add(StatusChangeResult.conflict(id, "Concurrent modification conflict"));
            }
        }
        return results;
    }
}
