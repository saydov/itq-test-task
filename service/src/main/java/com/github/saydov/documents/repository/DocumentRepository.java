package com.github.saydov.documents.repository;

import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.enums.DocumentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.stream.Stream;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    @Query(value = "SELECT nextval('document_number_seq')", nativeQuery = true)
    long nextDocumentNumber();

    Stream<Document> findByIdIn(List<Long> ids);

    List<Document> findTopByStatus(DocumentStatus status, Pageable pageable);

    long countByStatus(DocumentStatus status);
}
