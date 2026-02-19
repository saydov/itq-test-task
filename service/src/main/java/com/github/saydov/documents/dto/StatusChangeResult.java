package com.github.saydov.documents.dto;

import com.github.saydov.documents.enums.OperationStatus;

public record StatusChangeResult(Long id, OperationStatus status, String message) {

    public static StatusChangeResult success(Long id) {
        return new StatusChangeResult(id, OperationStatus.SUCCESS, null);
    }

    public static StatusChangeResult conflict(Long id, String message) {
        return new StatusChangeResult(id, OperationStatus.CONFLICT, message);
    }

    public static StatusChangeResult notFound(Long id, String message) {
        return new StatusChangeResult(id, OperationStatus.NOT_FOUND, message);
    }

    public static StatusChangeResult registryError(Long id, String message) {
        return new StatusChangeResult(id, OperationStatus.REGISTRY_ERROR, message);
    }

    public boolean isSuccess() {
        return status == OperationStatus.SUCCESS;
    }
}
