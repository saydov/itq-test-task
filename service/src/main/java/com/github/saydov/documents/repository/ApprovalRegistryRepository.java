package com.github.saydov.documents.repository;

import com.github.saydov.documents.entity.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {
}
