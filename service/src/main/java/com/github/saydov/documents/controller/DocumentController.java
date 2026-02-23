package com.github.saydov.documents.controller;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.dto.request.CreateDocumentRequest;
import com.github.saydov.documents.dto.request.DocumentSearchFilter;
import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.enums.DocumentAction;
import com.github.saydov.documents.service.DocumentService;
import com.github.saydov.documents.service.transition.DocumentTransitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentTransitionService transitionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDetailResponse createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.create(request);
    }

    @GetMapping("/{id}")
    public DocumentDetailResponse getDocument(@PathVariable Long id) {
        return documentService.getById(id);
    }

    @GetMapping("/batch")
    public List<DocumentDetailResponse> getDocuments(@RequestParam("ids") List<Long> ids) {
        return documentService.getByIds(ids);
    }

    @PostMapping("/{action}")
    public List<StatusChangeResult> executeAction(@PathVariable DocumentAction action, @Valid @RequestBody BatchStatusRequest request) {
        return transitionService.execute(action, request);
    }

    @GetMapping("/search")
    public Page<DocumentDetailResponse> searchDocuments(DocumentSearchFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return documentService.search(filter, pageable);
    }
}
