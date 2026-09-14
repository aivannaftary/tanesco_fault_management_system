package com.tanesco.faultmanagement.dto;

import jakarta.validation.constraints.NotNull;

public class AssignFaultRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    public AssignFaultRequest() {
    }

    public AssignFaultRequest(Long technicianId) {
        this.technicianId = technicianId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}