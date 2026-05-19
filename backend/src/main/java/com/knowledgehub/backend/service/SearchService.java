package com.knowledgehub.backend.service;

import com.knowledgehub.backend.repository.DocumentChunkRepository;
import com.knowledgehub.backend.dto.SearchRequest;
import com.knowledgehub.backend.dto.SearchResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final AiClientService aiClientService;
    private final DocumentChunkRepository chunkRepository;

    public SearchService(AiClientService aiClientService, DocumentChunkRepository chunkRepository) {
        this.aiClientService = aiClientService;
        this.chunkRepository = chunkRepository;
    }

    public SearchResponse performSemanticSearch(SearchRequest request) {
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

            SearchResponse response = new SearchResponse();
            List<SearchResponse.Source> sources = new ArrayList<>();
            StringBuilder contextBuilder = new StringBuilder();

            // Guard: If no context is found, return safely without calling LLM
            if (results == null || results.isEmpty()) {
                response.setAnswer("I don't know based on the provided documents. No relevant information was found.");
                response.setSources(sources);
                return response;
            }

            int chunkCount = 1;
            for (Object[] row : results) {
                String content = (String) row[0];
                Integer chunkIndex = (Integer) row[1];
                
                // Build the source list for UI citations
                SearchResponse.Source source = new SearchResponse.Source();
                source.setContent(content);
                source.setChunkIndex(chunkIndex);
                sources.add(source);

                // Build formatted context for LLM (Clean mental model)
                contextBuilder.append("[Chunk ").append(chunkCount).append("]\n")
                              .append(content).append("\n\n");
                chunkCount++;
            }

            // 4. Generate final human-like answer using RAG
            String generatedAnswer = aiClientService.generateFinalAnswer(request.getQuery(), contextBuilder.toString());

            response.setAnswer(generatedAnswer);
            response.setSources(sources);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Semantic search failed: " + e.getMessage());
        }
    }
}
