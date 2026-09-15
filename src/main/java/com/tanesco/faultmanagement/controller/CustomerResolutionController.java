package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.ConfirmResolutionRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.service.CustomerResolutionService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faults")
public class CustomerResolutionController {

    private final CustomerResolutionService service;

    public CustomerResolutionController(
            CustomerResolutionService service
    ) {
        this.service = service;
    }

    @PatchMapping("/{referenceNumber}/confirm-resolution")
    public ResponseEntity<FaultResponse>
    confirmResolution(
            @PathVariable String referenceNumber,
            @Valid @RequestBody
            ConfirmResolutionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.confirmResolution(
                        referenceNumber,
                        authentication.getName(),
                        request
                )
        );
    }
}