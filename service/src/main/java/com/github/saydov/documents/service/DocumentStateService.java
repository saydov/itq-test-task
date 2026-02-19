package com.github.saydov.documents.service;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.entity.ApprovalRegistry;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.entity.StatusHistory;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.exception.ApprovalRegistryException;
import com.github.saydov.documents.repository.ApprovalRegistryRepository;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentStateService {

    private final DocumentRepository documentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    private record TransitionContext(
            DocumentStatus targetStatus,
            String initiator,
            String comment,
            Consumer<Document> onSuccess
    ) {}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StatusChangeResult submitSingle(Long id, String initiator, String comment) {
        return transition(id, new TransitionContext(
                DocumentStatus.SUBMITTED, initiator, comment, entity -> {}));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StatusChangeResult approveSingle(Long id, String initiator, String comment) {
        return transition(id, new TransitionContext(
                DocumentStatus.APPROVED, initiator, comment,
                entity -> registerApproval(entity, initiator)));
    }

    private StatusChangeResult transition(Long id, TransitionContext context) {
        var result = documentRepository.findById(id)
                .map(entity -> {
                    var status = context.targetStatus();
                    if (!entity.getStatus().canTransitionTo(status)) {
                        return StatusChangeResult.conflict(id,
                                "Cannot transition from %s to %s".formatted(entity.getStatus(), status));
                    }

                    entity.setStatus(status);
                    statusHistoryRepository.save(StatusHistory.of(entity, context.initiator(),
                            status.getAction(), context.comment()));

                    context.onSuccess().accept(entity);
                    log.info("Document id={} transitioned to {}", id, status);
                    return StatusChangeResult.success(id);
                });

        return result.orElseGet(() -> StatusChangeResult.notFound(id, "Document not found"));
    }

    private void registerApproval(Document entity, String approvedBy) {
        try {
            approvalRegistryRepository.saveAndFlush(ApprovalRegistry.of(entity, approvedBy));
        } catch (DataIntegrityViolationException e) {
            throw new ApprovalRegistryException(entity.getId());
        }
    }
}
