package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.CreateStaffRequest;
import com.tanesco.faultmanagement.dto.StaffResponse;
import com.tanesco.faultmanagement.service.AdminStaffService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    public AdminStaffController(
            AdminStaffService adminStaffService
    ) {
        this.adminStaffService =
                adminStaffService;
    }

    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(
            @Valid @RequestBody
            CreateStaffRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminStaffService
                                .createStaff(request)
                );
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>>
    getStaff() {

        return ResponseEntity.ok(
                adminStaffService.getStaff()
        );
    }
}