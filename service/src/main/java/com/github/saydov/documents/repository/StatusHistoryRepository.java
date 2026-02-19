package com.github.saydov.documents.repository;

import com.github.saydov.documents.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}
