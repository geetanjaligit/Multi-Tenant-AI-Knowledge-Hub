package com.knowledgehub.backend.service;

import com.knowledgehub.backend.entity.User;
import com.knowledgehub.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String email, String password) {
        // Prevent duplicate usernames or emails
        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Username or Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // NOTE: For now, we are saving the raw password. 
        // We will add password hashing later when we do the full security layer!
        user.setPassword(password);

        return userRepository.save(user);
    }
}
