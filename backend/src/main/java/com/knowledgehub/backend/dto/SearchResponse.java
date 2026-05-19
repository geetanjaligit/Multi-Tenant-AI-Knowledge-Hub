package com.knowledgehub.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SearchResponse {
    private String answer;
    private List<Source> sources;

    @Data
    public static class Source {
        private String content;
        private Integer chunkIndex;
    }
}
