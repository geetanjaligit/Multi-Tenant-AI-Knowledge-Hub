package com.knowledgehub.backend.service;

import com.knowledgehub.backend.dto.AiProcessingResponse;
import com.knowledgehub.backend.entity.Document;
import com.knowledgehub.backend.entity.DocumentChunk;
import com.knowledgehub.backend.entity.KnowledgeSpace;
import com.knowledgehub.backend.repository.DocumentChunkRepository;
import com.knowledgehub.backend.repository.DocumentRepository;
import com.knowledgehub.backend.repository.KnowledgeSpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final KnowledgeSpaceRepository spaceRepository;
    private final DocumentChunkRepository chunkRepository;
    private final AiClientService aiClientService;

    public DocumentService(DocumentRepository documentRepository,
            KnowledgeSpaceRepository spaceRepository,
            DocumentChunkRepository chunkRepository,
            AiClientService aiClientService) {
        this.documentRepository = documentRepository;
        this.spaceRepository = spaceRepository;
        this.chunkRepository = chunkRepository;
        this.aiClientService = aiClientService;
    }

    public Document uploadDocument(MultipartFile file, Long spaceId, Long userId) throws IOException {
        KnowledgeSpace space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge Space not found"));

        if (!space.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not have permission to upload to this space");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setContent(content);
        document.setKnowledgeSpace(space);
        document.setUserId(userId); // Store userId directly
        document.setStatus(Document.DocumentStatus.UPLOADED);

        Document savedDoc = documentRepository.save(document);

        // 1. UPDATE STATUS TO PROCESSING
        savedDoc.setStatus(Document.DocumentStatus.PROCESSING);
        documentRepository.save(savedDoc);

        // 2. TRIGGER: Send to Python AI Service
        AiProcessingResponse aiResponse = aiClientService.sendToAiService(savedDoc);

        // 3. Save Chunks and Embeddings
        if (aiResponse != null && "success".equals(aiResponse.getStatus())) {
            for (AiProcessingResponse.ChunkData chunkData : aiResponse.getData()) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setContent(chunkData.getContent());
                chunk.setChunkIndex(chunkData.getChunkIndex());
                chunk.setDocument(savedDoc);
                
                // Convert List<Double> to String for temporary storage
                chunk.setEmbedding(chunkData.getEmbedding().toString());
                
                chunkRepository.save(chunk);
            }
            
            // 4. UPDATE STATUS TO INDEXED
            savedDoc.setStatus(Document.DocumentStatus.INDEXED);
            documentRepository.save(savedDoc);
            System.out.println("Saved " + aiResponse.getTotal_chunks() + " chunks with embeddings.");
        } else {
            // 5. UPDATE STATUS TO FAILED
            savedDoc.setStatus(Document.DocumentStatus.FAILED);
            documentRepository.save(savedDoc);
            System.err.println("AI Processing Failed for document: " + savedDoc.getId());
        }

        return savedDoc;
    }

    // Fetch documents for a space with multi-tenant validation
    public List<Document> getDocumentsBySpace(Long spaceId, Long userId) {
        return documentRepository.findByKnowledgeSpaceIdAndUserId(spaceId, userId);
    }

    public void updateDocumentStatus(Long docId, Document.DocumentStatus status) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        doc.setStatus(status);
        documentRepository.save(doc);
    }
}
