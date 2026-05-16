package com.knowledgehub.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_chunks")
@Data
@NoArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer chunkIndex; // To maintain the original order of the document

    @Column(columnDefinition = "TEXT")
    private String embedding; // Temporary TEXT storage

    @Column(columnDefinition = "vector(3072)")
    private float[] embeddingVector; // The real vector for pgvector

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}
