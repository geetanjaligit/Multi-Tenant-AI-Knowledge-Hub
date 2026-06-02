package com.knowledgehub.backend.service;

import com.knowledgehub.backend.repository.DocumentChunkRepository;
import com.knowledgehub.backend.repository.KnowledgeSpaceRepository;
import com.knowledgehub.backend.dto.SearchRequest;
import com.knowledgehub.backend.dto.SearchResponse;
import com.knowledgehub.backend.entity.KnowledgeSpace;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final AiClientService aiClientService;
    private final DocumentChunkRepository chunkRepository;
    private final KnowledgeSpaceRepository spaceRepository;

    public SearchService(
            AiClientService aiClientService,
            DocumentChunkRepository chunkRepository,
            KnowledgeSpaceRepository spaceRepository
    ) {
        this.aiClientService = aiClientService;
        this.chunkRepository = chunkRepository;
        this.spaceRepository = spaceRepository;
    }

    // userId now comes from the JWT token (passed in from the controller), NOT the request body
    public SearchResponse performSemanticSearch(SearchRequest request, Long userId) {
        try {
            // Step 1: SPACE OWNERSHIP VALIDATION
            // We check if the requested spaceId actually belongs to the authenticated userId.
            // This prevents user A from querying user B's private knowledge space.
            KnowledgeSpace space = spaceRepository.findById(request.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Knowledge space not found."));

            // The core security check: Does this space belong to the person asking?
            if (!space.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied: You do not own this knowledge space.");
            }

            // Step 2: GET VECTOR EMBEDDING FOR THE QUERY
            // Send the user's question text to the Python AI service
            // and get back a 3072-dimensional vector (a list of numbers representing the meaning)
            float[] queryVector = aiClientService.getQueryEmbedding(request.getQuery());
            
            // Step 3: CONVERT THE FLOAT ARRAY TO A STRING 
            // PostgreSQL pgvector expects a specific string format: "[0.1, 0.2, ...]"
            // This loop builds that string from the float array we received
            StringBuilder vectorString = new StringBuilder("[");
            for (int i = 0; i < queryVector.length; i++) {
                vectorString.append(queryVector[i]);
                if (i < queryVector.length - 1) vectorString.append(",");
            }
            vectorString.append("]");

            // Step 4: RUN SIMILARITY SEARCH IN POSTGRESQL 
            // Ask the database: "Find the 5 most similar chunks to my query vector,
            // but ONLY within this user's specific space."
            List<Object[]> results = chunkRepository.findSimilarContentWithDistance(
                    vectorString.toString(), 
                    userId,          
                    request.getSpaceId(), 
                    5
            );

            SearchResponse response = new SearchResponse();
            List<SearchResponse.Source> sources = new ArrayList<>();
            StringBuilder contextBuilder = new StringBuilder();

            // Step 5: GUARD AGAINST EMPTY RESULTS 
            // If no relevant chunks were found, return immediately without calling Gemini.
            // This prevents hallucination and saves API cost.
            if (results == null || results.isEmpty()) {
                response.setAnswer("I don't know based on the provided documents. No relevant information was found.");
                response.setSources(sources);
                return response;
            }

            // Step 6: BUILD THE CONTEXT AND SOURCE LIST 
            // Loop through each database result and build two things simultaneously:
            // (a) A formatted context string to send to Gemini for reading
            // (b) A sources list to send to the frontend for displaying citations
            int chunkCount = 1;
            for (Object[] row : results) {
                String content = (String) row[0];
                Integer chunkIndex = (Integer) row[1];
                
                // Build the source list for UI citations
                SearchResponse.Source source = new SearchResponse.Source();
                source.setContent(content);
                source.setChunkIndex(chunkIndex);
                sources.add(source);

                // Build formatted context for LLM (e.g., "[Chunk 1]\n...text...\n\n[Chunk 2]...")
                contextBuilder.append("[Chunk ").append(chunkCount).append("]\n")
                              .append(content).append("\n\n");
                chunkCount++;
            }

            // Step 7: GENERATE THE FINAL ANSWER USING RAG 
            // Send the question + the formatted context to Gemini.
            // Gemini reads the context and writes a clean, accurate, human-like answer.
            String generatedAnswer = aiClientService.generateFinalAnswer(request.getQuery(), contextBuilder.toString());

            response.setAnswer(generatedAnswer);
            response.setSources(sources);
            return response;

        } catch (RuntimeException e) {
            // Re-throw runtime exceptions (like our access denied error) as-is
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Semantic search failed: " + e.getMessage());
        }
    }
}
