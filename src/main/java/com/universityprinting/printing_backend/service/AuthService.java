package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.AuthResponse;
import com.universityprinting.printing_backend.dto.LoginRequest;
import com.universityprinting.printing_backend.dto.RegisterRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.exception.AuthenticationFailedException;
import com.universityprinting.printing_backend.exception.DuplicateEmailException;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.security.JwtService;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("User with email '" + normalizedEmail + "' already exists");
        }

        Instant now = Instant.now();
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }
}
