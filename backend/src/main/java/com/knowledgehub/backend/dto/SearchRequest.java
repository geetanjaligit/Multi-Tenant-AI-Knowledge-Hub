package com.knowledgehub.backend.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private Long spaceId;
    // NOTE: userId is intentionally removed. It will be extracted from the
    // secure JWT token, not trusted from the request body.
}
