package com.github.saydov.documents.service;

import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.dto.test.ConcurrencyTestRequest;
import com.github.saydov.documents.dto.test.ConcurrencyTestResponse;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.DocumentAction;
import com.github.saydov.documents.enums.OperationStatus;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.service.transition.DocumentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("test")
@RequiredArgsConstructor
public class ConcurrencyTestService {

    private final DocumentRepository documentRepository;
    private final DocumentTransitionService transitionService;

    public ConcurrencyTestResponse testConcurrentApproval(ConcurrencyTestRequest request) {
        log.info("Starting concurrency test: documentId={}, threads={}, attempts={}",
                request.getId(), request.getThreads(), request.getAttempts());

        var counters = new ResultCounters();
        var latch = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(request.getThreads())) {
            for (int i = 0; i < request.getAttempts(); i++) {
                executor.submit(() -> {
                    try {
                        latch.await();
                        var batchRequest = new BatchStatusRequest(
                                List.of(request.getId()), request.getInitiator(), null);

                        var results = transitionService.execute(DocumentAction.APPROVE, batchRequest);
                        results.forEach(r -> counters.increment(r.status()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            latch.countDown();
        }

        log.info("Concurrency test completed: {}", counters);
        return ConcurrencyTestResponse.builder()
                .id(request.getId())
                .totalAttempts(request.getAttempts())
                .successCount(counters.get(OperationStatus.SUCCESS))
                .conflictCount(counters.get(OperationStatus.CONFLICT))
                .errorCount(counters.get(OperationStatus.NOT_FOUND) + counters.get(OperationStatus.REGISTRY_ERROR))
                .finalStatus(documentRepository.findById(request.getId())
                        .map(Document::getStatus).orElse(null))
                .build();
    }

    private static class ResultCounters {
        private static final AtomicInteger ZERO = new AtomicInteger(0);
        private final Map<OperationStatus, AtomicInteger> counts = new EnumMap<>(OperationStatus.class);

        void increment(OperationStatus status) {
            counts.computeIfAbsent(status, k -> new AtomicInteger()).incrementAndGet();
        }

        int get(OperationStatus status) {
            return counts.getOrDefault(status, ZERO).get();
        }

        @Override
        public String toString() {
            return counts.entrySet().stream()
                    .map(e -> "%s=%d".formatted(e.getKey(), e.getValue().get()))
                    .collect(Collectors.joining(", "));
        }
    }
}
