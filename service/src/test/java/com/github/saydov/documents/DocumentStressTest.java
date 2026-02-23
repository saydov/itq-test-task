package com.github.saydov.documents;

import com.github.saydov.documents.configuration.TestcontainersConfig;
import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.DocumentAction;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.repository.ApprovalRegistryRepository;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.repository.StatusHistoryRepository;
import com.github.saydov.documents.service.transition.DocumentTransitionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@Tag("stress")
@SpringBootTest
@Import(TestcontainersConfig.class)
class DocumentStressTest {

    private static final String INITIATOR = "stress-test";

    @Autowired
    private DocumentTransitionService transitionService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ApprovalRegistryRepository approvalRegistryRepository;

    @BeforeEach
    void cleanup() {
        approvalRegistryRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @ParameterizedTest(name = "Полный цикл DRAFT → SUBMITTED → APPROVED для {0} документов")
    @ValueSource(ints = {5_000, 10_000, 50_000})
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void fullCycle(int count) {
        var watch = new StopWatch("stress-" + count);

        var ids = createDocuments(count, watch);
        assertThat(ids).hasSize(count);

        var submitResults = timed(watch, "submit",
                () -> transitionService.execute(DocumentAction.SUBMIT, new BatchStatusRequest(ids, INITIATOR, null)));

        assertSoftly(softly -> {
            softly.assertThat(submitResults).hasSize(count);
            softly.assertThat(submitResults).allMatch(StatusChangeResult::isSuccess);
        });

        List<StatusChangeResult> approveResults = timed(watch, "approve",
                () -> transitionService.execute(DocumentAction.APPROVE, new BatchStatusRequest(ids, INITIATOR, null)));

        assertSoftly(softly -> {
            softly.assertThat(approveResults).hasSize(count);
            softly.assertThat(approveResults).allMatch(StatusChangeResult::isSuccess);
        });

        timed(watch, "verify", () -> {
            assertSoftly(softly -> {
                softly.assertThat(documentRepository.countByStatus(DocumentStatus.APPROVED)).isEqualTo(count);
                softly.assertThat(approvalRegistryRepository.count()).isEqualTo(count);
            });
            return null;
        });

        log.info("[{}] {}", count, watch.prettyPrint());
    }

    private List<Long> createDocuments(int count, StopWatch watch) {
        return timed(watch, "create", () -> {
            var documents = new ArrayList<Document>(count);
            for (int i = 0; i < count; i++) {
                documents.add(Document.of("STRESS-%06d".formatted(i), "author-" + i, "title-" + i));
            }
            return documentRepository.saveAll(documents)
                    .stream()
                    .map(Document::getId)
                    .toList();
        });
    }

    private <T> T timed(StopWatch watch, String taskName, Supplier<T> action) {
        watch.start(taskName);
        try {
            return action.get();
        } finally {
            watch.stop();
        }
    }
}