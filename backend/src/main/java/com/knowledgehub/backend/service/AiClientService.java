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
}
