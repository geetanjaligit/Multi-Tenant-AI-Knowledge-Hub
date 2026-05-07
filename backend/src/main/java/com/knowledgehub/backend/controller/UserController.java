package com.knowledgehub.backend.controller;

import com.knowledgehub.backend.dto.CreateUserRequest;
import com.knowledgehub.backend.dto.UserResponse;
import com.knowledgehub.backend.entity.User;
import com.knowledgehub.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(mapToResponse(user));
    }

    // Helper method to safely convert User Entity to DTO (hiding the password)
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
