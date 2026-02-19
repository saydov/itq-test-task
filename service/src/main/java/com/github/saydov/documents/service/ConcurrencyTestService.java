package com.github.saydov.documents.service;

import com.github.saydov.documents.dto.test.ConcurrencyTestRequest;
import com.github.saydov.documents.dto.test.ConcurrencyTestResponse;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.OperationStatus;
import com.github.saydov.documents.exception.ApprovalRegistryException;
import com.github.saydov.documents.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("test")
@RequiredArgsConstructor
public class ConcurrencyTestService {

    private static final int EXECUTOR_TIMEOUT_SECONDS = 30;

    private final DocumentStateService stateService;
    private final DocumentRepository documentRepository;

    public ConcurrencyTestResponse testConcurrentApproval(ConcurrencyTestRequest request) {
        log.info("Starting concurrency test: documentId={}, threads={}, attempts={}",
                request.getId(), request.getThreads(), request.getAttempts());

        var counters = new ResultCounters();

        try (var executor = Executors.newFixedThreadPool(request.getThreads())) {
            var latch = new CountDownLatch(1);
            submitTasks(executor, latch, request, counters);
            latch.countDown();
            awaitCompletion(executor);
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

    private void submitTasks(ExecutorService executor, CountDownLatch latch, ConcurrencyTestRequest request, ResultCounters counters) {
        for (int i = 0; i < request.getAttempts(); i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    var result = stateService.approveSingle(
                            request.getId(), request.getInitiator(), null);
                    counters.increment(result.status());
                } catch (ObjectOptimisticLockingFailureException | ApprovalRegistryException e) {
                    counters.increment(OperationStatus.CONFLICT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    counters.increment(OperationStatus.REGISTRY_ERROR);
                }
            });
        }
    }

    private void awaitCompletion(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate within {} seconds", EXECUTOR_TIMEOUT_SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
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
