package com.knowledgehub.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiProcessingResponse {
    
    private String status;
    
    @JsonProperty("document_id")
    private Long documentId;
    
    @JsonProperty("total_chunks")
    private Integer totalChunks;
    
    private List<ChunkData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkData {
        private String content;
        private Integer chunkIndex;
        private List<Double> embedding;
    }
}
