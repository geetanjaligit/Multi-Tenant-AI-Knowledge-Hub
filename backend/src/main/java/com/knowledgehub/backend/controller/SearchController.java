package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.SearchRequest;
import com.knowledgehub.backend.dto.SearchResponse;
import com.knowledgehub.backend.service.JwtService;
import com.knowledgehub.backend.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<SearchResponse> search(
            @RequestBody SearchRequest request,
            HttpServletRequest httpRequest
    ) {
        // 1. Read the raw Authorization header from the incoming HTTP request
        // This header contains the value: "Bearer eyJhbGciOiJIUzI1NiIs..."
        String authHeader = httpRequest.getHeader("Authorization");

        // 2. Extract just the raw token by skipping the first 7 characters ("Bearer ")
        // Result: "eyJhbGciOiJIUzI1NiIs..."
        String jwt = authHeader.substring(7);

        // 3. Ask JwtService to crack open the token and read the userId claim from inside
        // This userId was embedded into the token by JwtService when the user first logged in
        Long userId = jwtService.extractUserId(jwt);

        // 4. Pass BOTH the userId (from token) and the spaceId (from request body) to the service
        SearchResponse results = searchService.performSemanticSearch(request, userId);

        return ResponseEntity.ok(results);
    }
}
