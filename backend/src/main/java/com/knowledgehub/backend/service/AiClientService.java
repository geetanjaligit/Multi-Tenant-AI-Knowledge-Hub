package com.knowledgehub.backend.service;

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
    public List<String> sendToAiService(Document document) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("document_id", document.getId());
        requestBody.put("content", document.getContent());

        try {
            // Send the POST request to Python
            // .postForObject is like clicking "Send" in Postman
            Map<String, Object> response = restTemplate.postForObject(AI_SERVICE_URL, requestBody, Map.class);
            
            System.out.println("--- Success: Python AI Service responded ---");
            
            // Extract the list of chunks from the JSON response
            return (List<String>) response.get("chunks");
            
        } catch (Exception e) {
            // If Python service is down, we log an error
            System.err.println("--- Error: Could not reach AI Service ---");
            System.err.println("Reason: " + e.getMessage());
            return null;
        }
    }
}
