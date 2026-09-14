package com.tanesco.faultmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResolveFaultRequest {

    @NotBlank(message = "Actual cause of the fault is required")
    @Size(
            max = 1000,
            message = "Actual cause must not exceed 1000 characters"
    )
    private String actualCause;

    @NotBlank(message = "Resolution notes are required")
    @Size(
            max = 3000,
            message = "Resolution notes must not exceed 3000 characters"
    )
    private String resolutionNotes;

    public ResolveFaultRequest() {
    }

    public ResolveFaultRequest(
            String actualCause,
            String resolutionNotes
    ) {
        this.actualCause = actualCause;
        this.resolutionNotes = resolutionNotes;
    }

    public String getActualCause() {
        return actualCause;
    }

    public void setActualCause(String actualCause) {
        this.actualCause = actualCause;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}