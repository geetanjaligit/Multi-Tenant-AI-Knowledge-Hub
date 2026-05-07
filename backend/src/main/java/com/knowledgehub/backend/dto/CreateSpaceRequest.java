package com.knowledgehub.backend.dto;

import lombok.Data;

@Data
public class CreateSpaceRequest {
    private Long userId;
    private String name;
    private String description;
}
