package com.github.saydov.documents.scheduler;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.service.DocumentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.workers.approve.enabled", havingValue = "true")
public class ApproveWorker extends AbstractDocumentWorker {

    protected ApproveWorker(DocumentService documentService) {
        super(documentService);
    }

    @Override
    public DocumentStatus getTargetStatus() {
        return DocumentStatus.SUBMITTED;
    }

    @Override
    public List<StatusChangeResult> applyStatusChange(List<Long> ids) {
        return documentService.approveDocuments(new BatchStatusRequest(ids, "APPROVE-WORKER", null));
    }

    @Scheduled(fixedDelayString = "${app.workers.approve.interval}")
    public void run() {
        processDocuments();
    }
}
