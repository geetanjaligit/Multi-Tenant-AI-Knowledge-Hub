package com.knowledgehub.backend.service;

import com.knowledgehub.backend.dto.AiProcessingResponse;
import com.knowledgehub.backend.entity.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("document_id", document.getId());
        requestBody.put("content", document.getContent());

        try {
            // Send the POST request to Python
            AiProcessingResponse response = restTemplate.postForObject(AI_SERVICE_URL, requestBody, AiProcessingResponse.class);
            
            System.out.println("--- Success: Python AI Service responded ---");
            return response;
            
        } catch (Exception e) {
            System.err.println("--- Error: Could not reach AI Service ---");
            System.err.println("Reason: " + e.getMessage());
            return null;
        }
    }
}
