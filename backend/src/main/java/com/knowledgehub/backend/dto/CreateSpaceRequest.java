package com.knowledgehub.backend.dto;

import lombok.Data;

@Data
public class CreateSpaceRequest {
    private String name;
    private String description;
}
