package com.knowledgehub.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class GenerateAnswerRequest {
    private String query;
    private String context;
}
