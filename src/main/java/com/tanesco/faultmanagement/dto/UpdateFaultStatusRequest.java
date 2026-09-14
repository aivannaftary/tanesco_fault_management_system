package com.tanesco.faultmanagement.dto;

import com.tanesco.faultmanagement.entity.FaultStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateFaultStatusRequest {

    @NotNull(message = "Fault status is required")
    private FaultStatus status;

    @NotBlank(message = "Update message is required")
    @Size(
            max = 2000,
            message = "Update message must not exceed 2000 characters"
    )
    private String message;

    public UpdateFaultStatusRequest() {
    }

    public UpdateFaultStatusRequest(
            FaultStatus status,
            String message
    ) {
        this.status = status;
        this.message = message;
    }

    public FaultStatus getStatus() {
        return status;
    }

    public void setStatus(FaultStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}