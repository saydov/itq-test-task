package com.github.saydov.documents.scheduler;

import com.github.saydov.documents.configuration.AppProperties;
import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.service.DocumentService;
import com.github.saydov.documents.service.transition.DocumentTransitionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDocumentWorker implements DocumentWorker {

    protected final DocumentService documentService;
    protected final DocumentTransitionService transitionService;
    private final AppProperties properties;

    protected void batchDocuments() {
        var status = getTargetStatus();
        var workerName = getClass().getSimpleName();

        var ids = documentService.findIdsByStatus(status, properties.getBatchSize());
        if (ids.isEmpty()) {
            return;
        }

        log.info("{}: processing {} {} documents", workerName, ids.size(), status);

        var start = System.currentTimeMillis();
        var results = applyStatusChange(ids);

        var success = results.stream().filter(StatusChangeResult::isSuccess).count();
        var elapsed = System.currentTimeMillis() - start;

        log.info("{}: batch completed in {}ms, success={}, failed={}",
                workerName, elapsed, success, results.size() - success);
    }
}
