package com.github.saydov.documents.repository;

import com.github.saydov.documents.entity.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long>,
        JpaSpecificationExecutor<ApprovalRegistry> {

    Optional<ApprovalRegistry> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);

    @Query("SELECT ar.document.id FROM ApprovalRegistry ar WHERE ar.document.id IN :documentIds")
    List<Long> findApprovedDocumentIds(Collection<Long> documentIds);

    void deleteByDocumentId(Long documentId);
}
