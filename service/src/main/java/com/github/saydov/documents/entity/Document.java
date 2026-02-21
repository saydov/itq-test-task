package com.github.saydov.documents.entity;

import com.github.saydov.documents.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
public class Document {

    @Contract("_, _, _, _ -> new")
    public static Document of(String number, String author, String title, DocumentStatus status) {
        var document = new Document();
        document.number = Objects.requireNonNull(number, "number must not be null");
        document.author = Objects.requireNonNull(author, "author must not be null");
        document.title = Objects.requireNonNull(title, "title must not be null");
        document.status = Objects.requireNonNull(status, "status must not be null");
        return document;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String title;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
