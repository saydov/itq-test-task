package com.github.saydov.documents.repository.domain;

import com.github.saydov.documents.dto.request.ApprovalSearchFilter;
import com.github.saydov.documents.entity.ApprovalRegistry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ApprovalRegistrySpecifications {

    private static final String APPROVED_BY = "approvedBy";
    private static final String APPROVED_AT = "approvedAt";

    private ApprovalRegistrySpecifications() {
        throw new UnsupportedOperationException();
    }

    public static Specification<ApprovalRegistry> fromFilter(ApprovalSearchFilter filter) {
        return Specification.where(approvedBy(filter.approvedBy()))
                .and(approvedAfter(filter.from()))
                .and(approvedBefore(filter.to()));
    }

    public static Specification<ApprovalRegistry> approvedBy(String approvedBy) {
        return optional(approvedBy, (root, query, cb) -> cb.equal(root.get(APPROVED_BY), approvedBy));
    }

    public static Specification<ApprovalRegistry> approvedAfter(LocalDateTime from) {
        return optional(from, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(APPROVED_AT), from));
    }

    public static Specification<ApprovalRegistry> approvedBefore(LocalDateTime to) {
        return optional(to, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(APPROVED_AT), to));
    }

    private static <T> Specification<ApprovalRegistry> optional(T value, Specification<ApprovalRegistry> spec) {
        return value == null ? Specification.where(null) : spec;
    }
}
