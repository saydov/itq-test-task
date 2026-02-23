package com.github.saydov.documents.controller;

import com.github.saydov.documents.dto.request.ApprovalSearchFilter;
import com.github.saydov.documents.dto.response.ApprovalRegistryResponse;
import com.github.saydov.documents.dto.response.ApprovalStatusResponse;
import com.github.saydov.documents.service.ApprovalRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalRegistryController {

    private final ApprovalRegistryService approvalService;

    @GetMapping("/document/{documentId}")
    public ApprovalRegistryResponse getByDocumentId(@PathVariable Long documentId) {
        return approvalService.getByDocumentId(documentId);
    }

    @GetMapping("/document/{documentId}/status")
    public ApprovalStatusResponse checkStatus(@PathVariable Long documentId) {
        return approvalService.checkStatus(documentId);
    }

    @GetMapping("/check")
    public List<ApprovalStatusResponse> checkStatuses(@RequestParam("ids") List<Long> documentIds) {
        return approvalService.checkStatuses(documentIds);
    }

    @GetMapping("/search")
    public Page<ApprovalRegistryResponse> search(ApprovalSearchFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return approvalService.search(filter, pageable);
    }

    @DeleteMapping("/document/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApproval(@PathVariable Long documentId) {
        approvalService.revoke(documentId);
    }
}
