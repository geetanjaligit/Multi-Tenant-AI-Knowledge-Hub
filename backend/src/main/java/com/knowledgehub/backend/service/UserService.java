package com.knowledgehub.backend.service;

import com.knowledgehub.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This service is reserved for future user management features (like getProfile, updatePassword, etc.)
}
