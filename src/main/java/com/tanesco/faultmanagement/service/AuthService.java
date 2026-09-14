package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.AuthResponse;
import com.tanesco.faultmanagement.dto.LoginRequest;
import com.tanesco.faultmanagement.dto.RegisterRequest;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.UserRepository;
import com.tanesco.faultmanagement.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String fullName = request.getFullName().trim();
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username is already in use."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered."
            );
        }

        User user = new User();

        user.setUsername(username);

        user.setFullName(fullName);

        user.setEmail(email);

        user.setPhone(phone);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(Role.CUSTOMER);

        User savedUser =
                userRepository.save(user);

        String token =
                jwtService.generateToken(
                        savedUser.getUsername(),
                        savedUser.getRole().name(),
                        savedUser.getFullName()
                );

        return new AuthResponse(
                token,
                "Bearer",
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        String username =
                request.getUsername().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account was not found."
                                )
                        );

        String token =
                jwtService.generateToken(
                        user.getUsername(),
                        user.getRole().name(),
                        user.getFullName()
                );

        return new AuthResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
