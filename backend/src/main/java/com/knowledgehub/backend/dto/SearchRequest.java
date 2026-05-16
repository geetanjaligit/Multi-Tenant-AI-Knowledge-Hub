package com.knowledgehub.backend.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private Long spaceId;
    private Long userId;
}
