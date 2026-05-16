package com.knowledgehub.backend.repository;

import com.knowledgehub.backend.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentId(Long documentId);

    @Query(value = "SELECT dc.content, (dc.embedding_vector <=> cast(:queryEmbedding as vector(3072))) as distance " +
                   "FROM document_chunks dc " +
                   "JOIN documents d ON dc.document_id = d.id " +
                   "WHERE d.user_id = :userId " +
                   "AND d.space_id = :spaceId " +
                   "AND (dc.embedding_vector <=> cast(:queryEmbedding as vector(3072))) < 0.4 " +
                   "ORDER BY distance ASC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findSimilarContentWithDistance(@Param("queryEmbedding") String queryEmbedding, 
                                                @Param("userId") Long userId, 
                                                @Param("spaceId") Long spaceId,
                                                @Param("limit") int limit);
}
