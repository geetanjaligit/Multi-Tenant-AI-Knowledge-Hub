package com.knowledgehub.backend.service;

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
    public void sendToAiService(Document document) {
        // 1. Prepare the Data (like a JSON object)
        // This MUST match the ProcessRequest class we wrote in Python (main.py)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("document_id", document.getId());
        requestBody.put("content", document.getContent());

        try {
            // 2. Send the POST request to Python
            // .postForObject is like clicking "Send" in Postman
            Map<String, Object> response = restTemplate.postForObject(AI_SERVICE_URL, requestBody, Map.class);
            
            System.out.println("--- Success: Python AI Service responded ---");
            System.out.println("Response: " + response);
            
        } catch (Exception e) {
            // If Python service is down, we log an error
            System.err.println("--- Error: Could not reach AI Service ---");
            System.err.println("Reason: " + e.getMessage());
        }
    }
}
