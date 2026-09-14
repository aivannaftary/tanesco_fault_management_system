package com.tanesco.faultmanagement.dto;

import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;

import java.time.LocalDateTime;

public class FaultResponse {

    private Long id;

    private String referenceNumber;

    private Long customerId;

    private String customerUsername;

    private String customerFullName;

    private Long technicianId;

    private String technicianUsername;

    private String technicianFullName;

    private String category;

    private String description;

    private String location;

    private String area;

    private Priority priority;

    private FaultStatus status;

    private String suspectedCause;

    private String actualCause;

    private String resolutionNotes;

    private LocalDateTime reportedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    private LocalDateTime updatedAt;

    public FaultResponse() {
    }

    public FaultResponse(
            Long id,
            String referenceNumber,
            Long customerId,
            String customerUsername,
            String customerFullName,
            Long technicianId,
            String technicianUsername,
            String technicianFullName,
            String category,
            String description,
            String location,
            String area,
            Priority priority,
            FaultStatus status,
            String suspectedCause,
            String actualCause,
            String resolutionNotes,
            LocalDateTime reportedAt,
            LocalDateTime assignedAt,
            LocalDateTime startedAt,
            LocalDateTime resolvedAt,
            LocalDateTime closedAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.customerId = customerId;
        this.customerUsername = customerUsername;
        this.customerFullName = customerFullName;
        this.technicianId = technicianId;
        this.technicianUsername = technicianUsername;
        this.technicianFullName = technicianFullName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.area = area;
        this.priority = priority;
        this.status = status;
        this.suspectedCause = suspectedCause;
        this.actualCause = actualCause;
        this.resolutionNotes = resolutionNotes;
        this.reportedAt = reportedAt;
        this.assignedAt = assignedAt;
        this.startedAt = startedAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianUsername() {
        return technicianUsername;
    }

    public void setTechnicianUsername(String technicianUsername) {
        this.technicianUsername = technicianUsername;
    }

    public String getTechnicianFullName() {
        return technicianFullName;
    }

    public void setTechnicianFullName(String technicianFullName) {
        this.technicianFullName = technicianFullName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public FaultStatus getStatus() {
        return status;
    }

    public void setStatus(FaultStatus status) {
        this.status = status;
    }

    public String getSuspectedCause() {
        return suspectedCause;
    }

    public void setSuspectedCause(String suspectedCause) {
        this.suspectedCause = suspectedCause;
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

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}