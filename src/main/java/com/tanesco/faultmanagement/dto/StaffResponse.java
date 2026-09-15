package com.tanesco.faultmanagement.dto;

import com.tanesco.faultmanagement.entity.Role;

public class StaffResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;

    public StaffResponse(
            Long id,
            String username,
            String fullName,
            String email,
            Role role
    ) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}