package com.github.saydov.documents.entity;

import com.github.saydov.documents.enums.DocumentAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "status_history")
@Getter
@Setter
@NoArgsConstructor
public class StatusHistory {

    @Contract("_, _, _, _ -> new")
    public static StatusHistory of(Document document, String initiator, DocumentAction action, String comment) {
        var history = new StatusHistory();
        history.setDocument(Objects.requireNonNull(document, "document must not be null"));
        history.setInitiator(Objects.requireNonNull(initiator, "initiator must not be null"));
        history.setAction(Objects.requireNonNull(action, "action must not be null"));
        history.setComment(comment);
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
