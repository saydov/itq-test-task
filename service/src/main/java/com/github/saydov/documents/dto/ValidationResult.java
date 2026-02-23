package com.github.saydov.documents.dto;

import java.util.List;

public record ValidationResult(List<Long> validIds, List<StatusChangeResult> failures) {
}
