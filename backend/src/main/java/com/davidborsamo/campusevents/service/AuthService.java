package com.davidborsamo.campusevents.service;

import com.davidborsamo.campusevents.dto.AuthResponse;
import com.davidborsamo.campusevents.dto.LoginRequest;
import com.davidborsamo.campusevents.dto.RegisterRequest;
import com.davidborsamo.campusevents.exception.ConflictException;
import com.davidborsamo.campusevents.model.User;
import com.davidborsamo.campusevents.repository.UserRepository;
import com.davidborsamo.campusevents.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        // Delegates the actual credential check to Spring Security's
        // AuthenticationManager, which uses the DaoAuthenticationProvider
        // configured in SecurityConfig — this is the framework-standard way
        // to verify a password rather than checking it manually.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished mid-request"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
