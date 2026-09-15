package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;
import com.tanesco.faultmanagement.service.FaultSearchService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/faults/search")
public class FaultSearchController {

    private final FaultSearchService faultSearchService;

    public FaultSearchController(
            FaultSearchService faultSearchService
    ) {
        this.faultSearchService =
                faultSearchService;
    }

    @GetMapping
    public ResponseEntity<List<FaultResponse>> search(
            @RequestParam(required = false)
            String referenceNumber,

            @RequestParam(required = false)
            FaultStatus status,

            @RequestParam(required = false)
            Priority priority,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            String area,

            @RequestParam(required = false)
            String location,

            @RequestParam(required = false)
            Long technicianId
    ) {

        return ResponseEntity.ok(
                faultSearchService.search(
                        referenceNumber,
                        status,
                        priority,
                        category,
                        area,
                        location,
                        technicianId
                )
        );
    }
}