package com.github.saydov.documents.service.transition;

import com.github.saydov.documents.configuration.AppProperties;
import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.StatusTransition;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.enums.DocumentAction;
import com.github.saydov.documents.service.persistence.DocumentLoader;
import com.github.saydov.documents.service.persistence.TransitionPersistenceService;
import com.github.saydov.documents.util.concurrent.Parallel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTransitionService {

    private final TransitionPersistenceService persistenceService;
    private final AppProperties.Transition transitionProperties;
    private final TransactionTemplate transactionTemplate;

    private final DocumentLoader documentLoader;
    private final DocumentTransitionValidator validator;

    public List<StatusChangeResult> execute(DocumentAction action, BatchStatusRequest request) {
        log.info("{}: {} documents, initiator={}", action, request.ids().size(), request.initiator());

        var results = executeTransition(request.ids(), action.getTransitionFactory().apply(request));
        var successCount = results.stream().filter(StatusChangeResult::isSuccess).count();

        log.info("{} completed: total={}, success={}, failed={}", action,
                results.size(), successCount, results.size() - successCount);
        return results;
    }

    private List<StatusChangeResult> executeTransition(List<Long> ids, StatusTransition transition) {
        var uniqueIds = new ArrayList<>(new LinkedHashSet<>(ids));

        var validation = validator.validate(uniqueIds, transition);
        log.debug("Validation: {} valid, {} rejected out of {} total",
                validation.validIds().size(), validation.failures().size(), uniqueIds.size());

        var batchResults = Parallel.batch(validation.validIds(), transitionProperties.batchSize())
                .concurrency(transitionProperties.maxConcurrency())
                .map(batch -> executeBatch(batch, transition));

        var resultStream = batchResults.stream().flatMap(Collection::stream);

        var resultsById = Stream.concat(validation.failures().stream(), resultStream)
                .collect(Collectors.toMap(StatusChangeResult::id, Function.identity()));

        return uniqueIds.stream().map(resultsById::get).toList();
    }

    private StatusChangeResult executeSingle(Long id, StatusTransition transition) {
        try {
            return executeInTransaction(List.of(id), transition).getFirst();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock conflict for document id={}", id);
            return StatusChangeResult.conflict(id, "Concurrent modification conflict");
        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation for document id={}: {}", id, e.getMessage());
            return StatusChangeResult.registryError(id, "Approval registry conflict");
        }
    }

    private List<StatusChangeResult> executeBatch(Collection<Long> ids, StatusTransition transition) {
        try {
            return executeInTransaction(ids, transition);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.warn("Batch failed ({}), falling back to single processing", e.getClass().getSimpleName());
            return ids.stream().map(id -> executeSingle(id, transition)).toList();
        }
    }

    private List<StatusChangeResult> executeInTransaction(Collection<Long> ids, StatusTransition transition) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            var documents = documentLoader.loadMap(ids);
            var collector = TransitionCollector.withCapacity(ids.size());

            for (var id : ids) {
                var doc = documents.get(id);
                validator.checkTransition(id, doc, transition)
                        .ifPresentOrElse(collector::failure, () -> collector.success(doc, transition));
            }

            persistenceService.flush(collector);
            return collector.results();
        }));
    }

}