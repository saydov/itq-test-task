package com.github.saydov.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "approval_registry")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRegistry {

    @Contract("_, _ -> new")
    public static ApprovalRegistry of(Document document, String approvedBy) {
        var registry = new ApprovalRegistry();
        registry.setDocument(Objects.requireNonNull(document, "document must not be null"));
        registry.setApprovedBy(Objects.requireNonNull(approvedBy, "approvedBy must not be null"));

        return registry;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "approved_by", nullable = false)
    private String approvedBy;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        approvedAt = LocalDateTime.now();
    }
}
