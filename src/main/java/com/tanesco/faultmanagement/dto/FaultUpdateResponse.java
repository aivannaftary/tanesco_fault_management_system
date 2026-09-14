package com.tanesco.faultmanagement.dto;

import com.tanesco.faultmanagement.entity.FaultStatus;

import java.time.LocalDateTime;

public class FaultUpdateResponse {

    private Long id;

    private FaultStatus status;

    private String message;

    private Long updatedByUserId;

    private String updatedByUsername;

    private String updatedByFullName;

    private LocalDateTime createdAt;

    public FaultUpdateResponse() {
    }

    public FaultUpdateResponse(
            Long id,
            FaultStatus status,
            String message,
            Long updatedByUserId,
            String updatedByUsername,
            String updatedByFullName,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.updatedByUserId = updatedByUserId;
        this.updatedByUsername = updatedByUsername;
        this.updatedByFullName = updatedByFullName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }

    public String getUpdatedByFullName() {
        return updatedByFullName;
    }

    public void setUpdatedByFullName(String updatedByFullName) {
        this.updatedByFullName = updatedByFullName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}