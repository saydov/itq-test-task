package com.github.saydov.documents.repository.domain;

import com.github.saydov.documents.dto.request.DocumentSearchFilter;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class DocumentSpecifications {

    private static final String STATUS = "status";
    private static final String AUTHOR = "author";
    private static final String CREATED_AT = "createdAt";

    private DocumentSpecifications() {
        throw new UnsupportedOperationException();
    }

    public static Specification<Document> fromFilter(DocumentSearchFilter filter) {
        return Specification.where(hasStatus(filter.status()))
                .and(hasAuthor(filter.author()))
                .and(createdAfter(filter.from()))
                .and(createdBefore(filter.to()));
    }

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return optional(status, (root, query, cb) -> cb.equal(root.get(STATUS), status));
    }

    public static Specification<Document> hasAuthor(String author) {
        return optional(author, (root, query, cb) -> cb.equal(root.get(AUTHOR), author));
    }

    public static Specification<Document> createdAfter(LocalDateTime from) {
        return optional(from, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), from));
    }

    public static Specification<Document> createdBefore(LocalDateTime to) {
        return optional(to, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(CREATED_AT), to));
    }

    private static <T> Specification<Document> optional(T value, Specification<Document> spec) {
        return value == null ? Specification.where(null) : spec;
    }
}