package com.knowledgehub.backend.service;

import com.knowledgehub.backend.dto.AiProcessingResponse;
import com.knowledgehub.backend.entity.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class AiClientService {

    private final RestTemplate restTemplate;
    
    // This is the URL where our Python server is running
    private final String AI_SERVICE_URL = "http://localhost:8000/process";

    public AiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This method sends document data to the Python AI service.
     */
    public AiProcessingResponse sendToAiService(Document document) {
        // (Existing code for processing documents)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("document_id", document.getId());
        requestBody.put("content", document.getContent());

        try {
            return restTemplate.postForObject(AI_SERVICE_URL, requestBody, AiProcessingResponse.class);
        } catch (Exception e) {
            System.err.println("--- Error: Could not reach AI Service ---");
            return null;
        }
    }

    /**
     * NEW: Generates an embedding for a user's search query.
     */
    public float[] getQueryEmbedding(String query) {
        String url = "http://localhost:8000/embed-query"; // Python service URL
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", query);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                List<Double> embeddingList = (List<Double>) response.get("embedding");
                float[] vector = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    vector[i] = embeddingList.get(i).floatValue();
                }
                return vector;
            }
        } catch (Exception e) {
            System.err.println("--- Error: Could not generate query embedding ---");
            System.err.println("Reason: " + e.getMessage());
        }
        return null;
    }

    /**
     * NEW: Generates the final human-readable answer using the Python RAG endpoint.
     */
    public String generateFinalAnswer(String query, String context) {
        String url = "http://localhost:8000/generate-answer";
        com.knowledgehub.backend.dto.GenerateAnswerRequest request = new com.knowledgehub.backend.dto.GenerateAnswerRequest(query, context);

        try {
            com.knowledgehub.backend.dto.GenerateAnswerResponse response = restTemplate.postForObject(url, request, com.knowledgehub.backend.dto.GenerateAnswerResponse.class);
            if (response != null && "success".equals(response.getStatus())) {
                return response.getAnswer();
            } else if (response != null) {
                System.err.println("--- Error from AI Service: " + response.getMessage() + " ---");
            }
        } catch (Exception e) {
            System.err.println("--- Error: Could not connect to AI Service for generation ---");
            System.err.println("Reason: " + e.getMessage());
        }
        return "Sorry, I am unable to generate an answer at this moment.";
    }
}
