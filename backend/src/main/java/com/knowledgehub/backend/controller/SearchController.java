package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.SearchRequest;
import com.knowledgehub.backend.dto.SearchResponse;
import com.knowledgehub.backend.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        SearchResponse results = searchService.performSemanticSearch(request);
        return ResponseEntity.ok(results);
    }
}
