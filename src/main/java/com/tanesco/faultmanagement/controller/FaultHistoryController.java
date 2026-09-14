package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.FaultUpdateResponse;
import com.tanesco.faultmanagement.service.FaultHistoryService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faults")
public class FaultHistoryController {

    private final FaultHistoryService faultHistoryService;

    public FaultHistoryController(
            FaultHistoryService faultHistoryService
    ) {
        this.faultHistoryService = faultHistoryService;
    }

    @GetMapping("/{referenceNumber}/history")
    public ResponseEntity<List<FaultUpdateResponse>> getFaultHistory(
            @PathVariable String referenceNumber,
            Authentication authentication
    ) {

        List<FaultUpdateResponse> history =
                faultHistoryService.getFaultHistory(
                        referenceNumber,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                history
        );
    }
}