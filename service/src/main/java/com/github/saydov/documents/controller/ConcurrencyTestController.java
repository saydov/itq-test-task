package com.github.saydov.documents.controller;

import com.github.saydov.documents.dto.test.ConcurrencyTestRequest;
import com.github.saydov.documents.dto.test.ConcurrencyTestResponse;
import com.github.saydov.documents.service.ConcurrencyTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class ConcurrencyTestController {

    private final ConcurrencyTestService concurrencyTestService;

    @PostMapping("/concurrency-test")
    public ConcurrencyTestResponse testConcurrentApproval(@Valid @RequestBody ConcurrencyTestRequest request) {
        return concurrencyTestService.testConcurrentApproval(request);
    }
}
