package com.github.saydov.documents.service;

import com.github.saydov.documents.configuration.AppProperties;
import com.github.saydov.documents.dto.request.ApprovalSearchFilter;
import com.github.saydov.documents.dto.response.ApprovalRegistryResponse;
import com.github.saydov.documents.dto.response.ApprovalStatusResponse;
import com.github.saydov.documents.entity.ApprovalRegistry;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.exception.ApprovalNotFoundException;
import com.github.saydov.documents.mapper.DocumentMapper;
import com.github.saydov.documents.repository.ApprovalRegistryRepository;
import com.github.saydov.documents.repository.domain.ApprovalRegistrySpecifications;
import com.github.saydov.documents.util.concurrent.Parallel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRegistryService {

    private final ApprovalRegistryRepository registryRepository;
    private final DocumentMapper mapper;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public ApprovalRegistryResponse getByDocumentId(Long documentId) {
        return registryRepository.findByDocumentId(documentId)
                .map(mapper::buildApprovalResponse)
                .orElseThrow(() -> new ApprovalNotFoundException(documentId));
    }

    @Transactional(readOnly = true)
    public ApprovalStatusResponse checkStatus(Long documentId) {
        return new ApprovalStatusResponse(documentId, registryRepository.existsByDocumentId(documentId));
    }

    @Transactional(readOnly = true)
    public List<ApprovalStatusResponse> checkStatuses(Collection<Long> documentIds) {
        var approvedIds = new HashSet<>(
                Parallel.batch(documentIds, properties.getBatchSize())
                        .map(registryRepository::findApprovedDocumentIds)
                        .stream()
                        .flatMap(Collection::stream)
                        .toList()
        );

        return documentIds.stream()
                .map(id -> new ApprovalStatusResponse(id, approvedIds.contains(id)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRegistryResponse> search(ApprovalSearchFilter filter, Pageable pageable) {
        return registryRepository.findAll(ApprovalRegistrySpecifications.fromFilter(filter), pageable)
                .map(mapper::buildApprovalResponse);
    }

    @Transactional(readOnly = true)
    public List<Long> findApprovedIds(Collection<Long> documentIds) {
        return registryRepository.findApprovedDocumentIds(documentIds);
    }

    @Transactional
    public void saveAll(List<ApprovalRegistry> approvals) {
        registryRepository.saveAll(approvals);
    }

    @Transactional
    public void revoke(Long documentId) {
        var approval = registryRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new ApprovalNotFoundException(documentId));

        registryRepository.delete(approval);
        approval.getDocument().setStatus(DocumentStatus.SUBMITTED);
        log.info("Revoked approval for documentId={}", documentId);
    }
}
