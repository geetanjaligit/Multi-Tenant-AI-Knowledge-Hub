package com.knowledgehub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSpaceResponse {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private LocalDateTime createdAt;
}
