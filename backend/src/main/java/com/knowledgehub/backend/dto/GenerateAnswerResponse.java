package com.knowledgehub.backend.dto;

import lombok.Data;

@Data
public class GenerateAnswerResponse {
    private String status;
    private String answer;
    private String message;
}
