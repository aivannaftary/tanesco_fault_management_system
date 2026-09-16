package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.StaffResponse;
import com.tanesco.faultmanagement.service.StaffDirectoryService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffDirectoryController {

    private final StaffDirectoryService staffDirectoryService;

    public StaffDirectoryController(
            StaffDirectoryService staffDirectoryService
    ) {
        this.staffDirectoryService =
                staffDirectoryService;
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public ResponseEntity<List<StaffResponse>>
    getTechnicians() {

        return ResponseEntity.ok(
                staffDirectoryService.getTechnicians()
        );
    }
}
