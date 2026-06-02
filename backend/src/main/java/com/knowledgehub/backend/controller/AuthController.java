package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.AuthenticationRequest;
import com.knowledgehub.backend.dto.AuthenticationResponse;
import com.knowledgehub.backend.dto.RegisterRequest;
import com.knowledgehub.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") 
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint to register a new user.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        // Pass the JSON data to the service and return the resulting token
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /** 
     * Endpoint to log in an existing user.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // Pass the JSON data to the service and return the resulting token
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
