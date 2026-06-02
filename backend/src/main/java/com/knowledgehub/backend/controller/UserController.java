package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    
    // Future endpoints (like GET /api/users/profile) will go here.
}
