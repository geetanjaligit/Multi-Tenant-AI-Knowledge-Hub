package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.DocumentResponse;
import com.knowledgehub.backend.entity.Document;
import com.knowledgehub.backend.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("spaceId") Long spaceId,
            @RequestParam("userId") Long userId) {
        
        try {
            Document doc = documentService.uploadDocument(file, spaceId, userId);
            return ResponseEntity.ok(mapToResponse(doc));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }

    //list documents in a space for a specific user
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsBySpace(
            @PathVariable Long spaceId,
            @RequestParam("userId") Long userId) {
        
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
