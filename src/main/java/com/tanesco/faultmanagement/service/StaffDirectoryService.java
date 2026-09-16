package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.StaffResponse;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffDirectoryService {

    private final UserRepository userRepository;

    public StaffDirectoryService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getTechnicians() {

        return userRepository
                .findByRole(Role.TECHNICIAN)
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
