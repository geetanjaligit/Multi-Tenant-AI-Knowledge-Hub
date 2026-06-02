package com.knowledgehub.backend.service;

import com.knowledgehub.backend.dto.AuthenticationRequest;
import com.knowledgehub.backend.dto.AuthenticationResponse;
import com.knowledgehub.backend.dto.RegisterRequest;
import com.knowledgehub.backend.entity.Role;
import com.knowledgehub.backend.entity.User;
import com.knowledgehub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Handles creating a new user account.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        
        // 1. Create an empty User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // 2. Encrypt the raw password before saving it to the database
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 3. Assign the default Role
        user.setRole(Role.USER);

        // 4. Save the user securely into the PostgreSQL database
        userRepository.save(user);

        // 5. Generate a brand new JWT token for this user
        String jwtToken = jwtService.generateToken(user);
        
        // 6. Return the token inside our Response DTO
        return new AuthenticationResponse(jwtToken);
    }

    /**
     * Handles verifying existing users.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        
        // 1. We ask Spring's AuthenticationManager to verify the password.
        // If the password is wrong, this line will throw an error and stop the code immediately.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. If the code reaches here, the password was perfectly correct!
        // We fetch the User from the database so we have their ID and Role.
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // 3. Generate a fresh 24-hour JWT token for them
        String jwtToken = jwtService.generateToken(user);
        
        // 4. Return the token
        return new AuthenticationResponse(jwtToken);
    }
}
