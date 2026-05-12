package com.knowledgehub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiProcessingResponse {
    private String status;
    private Long document_id;
    private Integer total_chunks;
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
