package com.knowledgehub.backend.service;

import com.knowledgehub.backend.entity.Document;
import com.knowledgehub.backend.entity.KnowledgeSpace;
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

    public DocumentService(DocumentRepository documentRepository, KnowledgeSpaceRepository spaceRepository) {
        this.documentRepository = documentRepository;
        this.spaceRepository = spaceRepository;
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

        return documentRepository.save(document);
    }

    //Fetch documents for a space with multi-tenant validation
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
