package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.CreateSpaceRequest;
import com.knowledgehub.backend.dto.KnowledgeSpaceResponse;
import com.knowledgehub.backend.entity.KnowledgeSpace;
import com.knowledgehub.backend.service.JwtService;
import com.knowledgehub.backend.service.KnowledgeSpaceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class KnowledgeSpaceController {

    private final KnowledgeSpaceService spaceService;
    private final JwtService jwtService;

    // extract userId from the JWT token on every request
    private Long extractUserIdFromRequest(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String jwt = authHeader.substring(7); // Strip "Bearer "
        return jwtService.extractUserId(jwt);
    }

    // Endpoint to create a new Knowledge Space
    @PostMapping
    public ResponseEntity<KnowledgeSpaceResponse> createSpace(
            @RequestBody CreateSpaceRequest request,
            HttpServletRequest httpRequest
    ) {
        // Extract the real userId from the JWT — this CANNOT be faked
        Long userId = extractUserIdFromRequest(httpRequest);
        KnowledgeSpace space = spaceService.createSpace(userId, request.getName(), request.getDescription());
        return ResponseEntity.ok(mapToResponse(space));
    }

    // Endpoint to fetch all spaces belonging to the authenticated user
    @GetMapping
    public ResponseEntity<List<KnowledgeSpaceResponse>> getUserSpaces(HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromRequest(httpRequest);
        List<KnowledgeSpace> spaces = spaceService.getUserSpaces(userId);
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
                space.getUser().getId(),
                space.getCreatedAt()
        );
    }
}
