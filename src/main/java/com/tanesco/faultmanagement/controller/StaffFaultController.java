package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.AssignFaultRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.dto.ResolveFaultRequest;
import com.tanesco.faultmanagement.dto.UpdateFaultStatusRequest;
import com.tanesco.faultmanagement.service.FaultManagementService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/faults")
public class StaffFaultController {

    private final FaultManagementService faultManagementService;

    public StaffFaultController(
            FaultManagementService faultManagementService
    ) {
        this.faultManagementService = faultManagementService;
    }

    @PatchMapping("/{referenceNumber}/assign")
    public ResponseEntity<FaultResponse> assignFault(
            @PathVariable String referenceNumber,
            @Valid @RequestBody AssignFaultRequest request,
            Authentication authentication
    ) {

        FaultResponse response =
                faultManagementService.assignFault(
                        referenceNumber,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{referenceNumber}/status")
    public ResponseEntity<FaultResponse> updateStatus(
            @PathVariable String referenceNumber,
            @Valid @RequestBody UpdateFaultStatusRequest request,
            Authentication authentication
    ) {

        FaultResponse response =
                faultManagementService.updateStatus(
                        referenceNumber,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{referenceNumber}/resolve")
    public ResponseEntity<FaultResponse> resolveFault(
            @PathVariable String referenceNumber,
            @Valid @RequestBody ResolveFaultRequest request,
            Authentication authentication
    ) {

        FaultResponse response =
                faultManagementService.resolveFault(
                        referenceNumber,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}
