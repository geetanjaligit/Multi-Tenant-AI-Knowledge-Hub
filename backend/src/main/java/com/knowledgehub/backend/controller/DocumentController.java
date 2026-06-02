package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.DocumentResponse;
import com.knowledgehub.backend.entity.Document;
import com.knowledgehub.backend.service.DocumentService;
import com.knowledgehub.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final JwtService jwtService;

    // Helper method: extract userId from the JWT token on every request
    private Long extractUserIdFromRequest(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String jwt = authHeader.substring(7); // Strip "Bearer "
        return jwtService.extractUserId(jwt);
    }

    // Upload a document — userId comes from the JWT, NOT the form param
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("spaceId") Long spaceId,
            HttpServletRequest httpRequest
    ) {
        try {
            // Extract the real userId from the JWT — this CANNOT be faked
            Long userId = extractUserIdFromRequest(httpRequest);
            Document doc = documentService.uploadDocument(file, spaceId, userId);
            return ResponseEntity.ok(mapToResponse(doc));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // List documents in a space — userId comes from the JWT, NOT the query param
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsBySpace(
            @PathVariable Long spaceId,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserIdFromRequest(httpRequest);
        List<Document> documents = documentService.getDocumentsBySpace(spaceId, userId);
        List<DocumentResponse> response = documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private DocumentResponse mapToResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getFileType(),
                doc.getFileSize(),
                doc.getStatus().name(),
                doc.getKnowledgeSpace().getId(),
                doc.getUserId(),
                doc.getCreatedAt()
        );
    }
}
