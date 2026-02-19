package com.github.saydov.documents.controller;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.dto.request.CreateDocumentRequest;
import com.github.saydov.documents.dto.request.DocumentSearchFilter;
import com.github.saydov.documents.dto.response.DocumentDetailResponse;
import com.github.saydov.documents.dto.response.DocumentResponse;
import com.github.saydov.documents.service.DocumentService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.createDocument(request);
    }

    @GetMapping("/{id}")
    public DocumentDetailResponse getDocument(@PathVariable("id") Long id) {
        return documentService.getDocument(id);
    }

    @GetMapping("/batch")
    public List<DocumentResponse> getDocuments(@RequestParam("ids") List<Long> ids) {
        return documentService.getDocuments(ids);
    }

    @PostMapping("/submit")
    public List<StatusChangeResult> submitDocuments(@Valid @RequestBody BatchStatusRequest request) {
        return documentService.submitDocuments(request);
    }

    @PostMapping("/approve")
    public List<StatusChangeResult> approveDocuments(@Valid @RequestBody BatchStatusRequest request) {
        return documentService.approveDocuments(request);
    }

    @GetMapping("/search")
    public Page<DocumentResponse> searchDocuments(DocumentSearchFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return documentService.searchDocuments(filter, pageable);
    }
}
