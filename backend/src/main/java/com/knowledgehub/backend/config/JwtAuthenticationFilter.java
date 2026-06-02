package com.knowledgehub.backend.config;

import com.knowledgehub.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @SuppressWarnings("deprecation")
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Check if the Authorization header is missing or doesn't have the "Bearer " prefix.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the actual token string (skipping the "Bearer " part which is 7 characters long)
        jwt = authHeader.substring(7);
        
        // 3. Extract the username from the token using our JwtService
        username = jwtService.extractUsername(jwt);

        // 4. If the username exists and the user is NOT yet authenticated in the current session
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 5. Load the user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Check if the token is valid for this specific user
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 7. Create a valid authentication object using the user's details and roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                // 8. Add extra request details (like IP address) to the authentication object
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 9. Tell Spring Security context: "This user is now officially authenticated!"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 10. Pass the request forward to the next filter (or to the Controller if we are done)
        filterChain.doFilter(request, response);
    }
}
