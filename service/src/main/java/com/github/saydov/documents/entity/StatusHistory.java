package com.github.saydov.documents.entity;

import com.github.saydov.documents.enums.DocumentAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "status_history")
@Getter
@NoArgsConstructor
public class StatusHistory {

    @Contract("_, _, _, _ -> new")
    public static StatusHistory of(Document document, String initiator, DocumentAction action, String comment) {
        var history = new StatusHistory();
        history.document = Objects.requireNonNull(document, "document cannot be null");
        history.initiator = Objects.requireNonNull(initiator, "initiator cannot be null");
        history.action = Objects.requireNonNull(action, "action cannot be null");
        history.comment = comment;
        return history;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private String initiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentAction action;

    @Column
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
