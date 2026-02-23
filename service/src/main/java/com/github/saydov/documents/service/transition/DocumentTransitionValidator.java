package com.github.saydov.documents.service.transition;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.StatusTransition;
import com.github.saydov.documents.dto.ValidationResult;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.service.ApprovalRegistryService;
import com.github.saydov.documents.service.persistence.DocumentLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DocumentTransitionValidator {

    private final DocumentLoader documentLoader;
    private final ApprovalRegistryService approvalService;

    @Transactional(readOnly = true)
    public ValidationResult validate(Collection<Long> ids, StatusTransition transition) {
        var documents = documentLoader.loadMap(ids);
        var approvedIds = loadApprovedIds(ids, transition);

        var validIds = new ArrayList<Long>(ids.size());
        var failures = new ArrayList<StatusChangeResult>();

        for (var id : ids) {
            checkTransition(id, documents.get(id), transition)
                    .or(() -> checkNotAlreadyApproved(id, approvedIds))
                    .ifPresentOrElse(failures::add, () -> validIds.add(id));
        }

        return new ValidationResult(validIds, failures);
    }

    public Optional<StatusChangeResult> checkTransition(Long id, Document doc, StatusTransition transition) {
        if (doc == null) {
            return Optional.of(StatusChangeResult.notFound(id, "Document not found"));
        }
        if (!doc.getStatus().canTransitionTo(transition.targetStatus())) {
            return Optional.of(StatusChangeResult.conflict(id,
                    "Cannot transition from %s to %s".formatted(doc.getStatus(), transition.targetStatus())));
        }
        return Optional.empty();
    }

    private Optional<StatusChangeResult> checkNotAlreadyApproved(Long id, Set<Long> approvedIds) {
        if (approvedIds.contains(id)) {
            return Optional.of(StatusChangeResult.registryError(id, "Document already approved"));
        }
        return Optional.empty();
    }

    private Set<Long> loadApprovedIds(Collection<Long> ids, StatusTransition transition) {
        return transition.isApproval() ? new HashSet<>(approvalService.findApprovedIds(ids)) : Set.of();
    }
}