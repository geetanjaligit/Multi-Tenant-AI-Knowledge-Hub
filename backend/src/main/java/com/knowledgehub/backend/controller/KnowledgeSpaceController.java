package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.CreateSpaceRequest;
import com.knowledgehub.backend.dto.KnowledgeSpaceResponse;
import com.knowledgehub.backend.entity.KnowledgeSpace;
import com.knowledgehub.backend.service.KnowledgeSpaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
public class KnowledgeSpaceController {

    private final KnowledgeSpaceService spaceService;

    public KnowledgeSpaceController(KnowledgeSpaceService spaceService) {
        this.spaceService = spaceService;
    }

    // Endpoint to create a new Knowledge Space for a specific user
    @PostMapping
    public ResponseEntity<KnowledgeSpaceResponse> createSpace(@RequestBody CreateSpaceRequest request) {
        KnowledgeSpace space = spaceService.createSpace(request.getUserId(), request.getName(), request.getDescription());
        return ResponseEntity.ok(mapToResponse(space));
    }

    // Endpoint to fetch spaces, ensuring we only get them for the requested user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KnowledgeSpaceResponse>> getUserSpaces(@PathVariable Long userId) {
        List<KnowledgeSpace> spaces = spaceService.getUserSpaces(userId);
        
        // Convert the list of entities into a list of safe DTOs
        List<KnowledgeSpaceResponse> response = spaces.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }
    
    // Helper method to map the Database Entity to the safe Response DTO
    private KnowledgeSpaceResponse mapToResponse(KnowledgeSpace space) {
        return new KnowledgeSpaceResponse(
                space.getId(),
                space.getName(),
                space.getDescription(),
                space.getUser().getId(), // Instead of the whole user object, just return the ID
                space.getCreatedAt()
        );
    }
}
