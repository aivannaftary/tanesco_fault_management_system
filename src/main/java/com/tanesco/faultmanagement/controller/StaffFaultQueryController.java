package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.service.StaffFaultQueryService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/faults")
public class StaffFaultQueryController {

    private final StaffFaultQueryService staffFaultQueryService;

    public StaffFaultQueryController(
            StaffFaultQueryService staffFaultQueryService
    ) {
        this.staffFaultQueryService = staffFaultQueryService;
    }

    @GetMapping
    public ResponseEntity<List<FaultResponse>> getFaults(
            @RequestParam(required = false) FaultStatus status
    ) {

        List<FaultResponse> faults;

        if (status == null) {

            faults =
                    staffFaultQueryService.getAllFaults();

        } else {

            faults =
                    staffFaultQueryService.getFaultsByStatus(
                            status
                    );
        }

        return ResponseEntity.ok(faults);
    }
}
