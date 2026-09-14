package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.FaultRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.service.FaultService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultService faultService;

    public FaultController(
            FaultService faultService
    ) {
        this.faultService = faultService;
    }

    @PostMapping
    public ResponseEntity<FaultResponse> createFault(
            @Valid @RequestBody FaultRequest request,
            Authentication authentication
    ) {

        FaultResponse response =
                faultService.createFault(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FaultResponse>> getMyFaults(
            Authentication authentication
    ) {

        List<FaultResponse> faults =
                faultService.getCustomerFaults(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                faults
        );
    }

    @GetMapping("/{referenceNumber}")
    public ResponseEntity<FaultResponse> getFault(
            @PathVariable String referenceNumber,
            Authentication authentication
    ) {

        FaultResponse response =
                faultService.getFaultByReferenceNumber(
                        referenceNumber,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                response
        );
    }
}