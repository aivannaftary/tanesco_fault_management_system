package com.tanesco.faultmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConfirmResolutionRequest {

    @NotBlank
    @Size(max = 1000)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message = message;
    }
}