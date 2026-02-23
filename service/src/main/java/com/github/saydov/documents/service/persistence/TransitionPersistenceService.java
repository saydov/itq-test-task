package com.github.saydov.documents.service.persistence;

import com.github.saydov.documents.repository.StatusHistoryRepository;
import com.github.saydov.documents.service.ApprovalRegistryService;
import com.github.saydov.documents.service.transition.TransitionCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransitionPersistenceService {

    private final StatusHistoryRepository historyRepository;
    private final ApprovalRegistryService approvalService;

    public void flush(TransitionCollector collector) {
        collector.drainTo(historyRepository::saveAll, approvalService::saveAll);
    }
}