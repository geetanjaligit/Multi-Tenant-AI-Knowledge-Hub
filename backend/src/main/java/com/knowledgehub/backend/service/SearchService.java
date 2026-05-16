package com.knowledgehub.backend.service;

import com.knowledgehub.backend.entity.DocumentChunk;
import com.knowledgehub.backend.repository.DocumentChunkRepository;
import com.knowledgehub.backend.dto.SearchRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final AiClientService aiClientService;
    private final DocumentChunkRepository chunkRepository;

    public SearchService(AiClientService aiClientService, DocumentChunkRepository chunkRepository) {
        this.aiClientService = aiClientService;
        this.chunkRepository = chunkRepository;
    }

    public List<String> performSemanticSearch(SearchRequest request) {
        try {
            // 1. Get Vector for the User's Query from Python
            float[] queryVector = aiClientService.getQueryEmbedding(request.getQuery());
            
            // 2. Convert float[] to string format: "[0.1, 0.2, ...]"
            StringBuilder vectorString = new StringBuilder("[");
            for (int i = 0; i < queryVector.length; i++) {
                vectorString.append(queryVector[i]);
                if (i < queryVector.length - 1) vectorString.append(",");
            }
            vectorString.append("]");

            // 3. Perform Similarity Search
            List<Object[]> results = chunkRepository.findSimilarContentWithDistance(
                    vectorString.toString(), 
                    request.getUserId(), 
                    request.getSpaceId(), 
                    5
            );

            return results.stream()
                    .map(row -> (String) row[0])
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Semantic search failed: " + e.getMessage());
        }
    }
}
