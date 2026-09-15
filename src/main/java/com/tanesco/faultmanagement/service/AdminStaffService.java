package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.CreateStaffRequest;
import com.tanesco.faultmanagement.dto.StaffResponse;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminStaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminStaffService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StaffResponse createStaff(
            CreateStaffRequest request
    ) {

        if (request.getRole() != Role.OFFICER
                && request.getRole() != Role.TECHNICIAN
                && request.getRole() != Role.ADMIN) {

            throw new IllegalArgumentException(
                    "Staff role must be OFFICER, TECHNICIAN or ADMIN."
            );
        }

        if (userRepository.existsByUsername(
                request.getUsername()
        )) {

            throw new IllegalStateException(
                    "Username is already in use."
            );
        }

        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new IllegalStateException(
                    "Email is already in use."
            );
        }

        User user =
                new User();

        user.setUsername(
                request.getUsername().trim()
        );

        user.setFullName(
                request.getFullName().trim()
        );

        user.setEmail(
                request.getEmail().trim()
        );

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                request.getRole()
        );

        User saved =
                userRepository.save(user);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff() {

        return userRepository
                .findByRoleIn(
                        List.of(
                                Role.ADMIN,
                                Role.OFFICER,
                                Role.TECHNICIAN
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StaffResponse toResponse(
            User user
    ) {

        return new StaffResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
