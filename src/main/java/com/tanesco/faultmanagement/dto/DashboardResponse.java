package com.tanesco.faultmanagement.dto;

public class DashboardResponse {

    private long totalFaults;
    private long submittedFaults;
    private long underReviewFaults;
    private long assignedFaults;
    private long inProgressFaults;
    private long resolvedFaults;
    private long closedFaults;
    private long rejectedFaults;

    private long highPriorityFaults;
    private long criticalPriorityFaults;

    public DashboardResponse() {
    }

    public DashboardResponse(
            long totalFaults,
            long submittedFaults,
            long underReviewFaults,
            long assignedFaults,
            long inProgressFaults,
            long resolvedFaults,
            long closedFaults,
            long rejectedFaults,
            long highPriorityFaults,
            long criticalPriorityFaults
    ) {
        this.totalFaults = totalFaults;
        this.submittedFaults = submittedFaults;
        this.underReviewFaults = underReviewFaults;
        this.assignedFaults = assignedFaults;
        this.inProgressFaults = inProgressFaults;
        this.resolvedFaults = resolvedFaults;
        this.closedFaults = closedFaults;
        this.rejectedFaults = rejectedFaults;
        this.highPriorityFaults = highPriorityFaults;
        this.criticalPriorityFaults = criticalPriorityFaults;
    }

    public long getTotalFaults() {
        return totalFaults;
    }

    public void setTotalFaults(long totalFaults) {
        this.totalFaults = totalFaults;
    }

    public long getSubmittedFaults() {
        return submittedFaults;
    }

    public void setSubmittedFaults(long submittedFaults) {
        this.submittedFaults = submittedFaults;
    }

    public long getUnderReviewFaults() {
        return underReviewFaults;
    }

    public void setUnderReviewFaults(long underReviewFaults) {
        this.underReviewFaults = underReviewFaults;
    }

    public long getAssignedFaults() {
        return assignedFaults;
    }

    public void setAssignedFaults(long assignedFaults) {
        this.assignedFaults = assignedFaults;
    }

    public long getInProgressFaults() {
        return inProgressFaults;
    }

    public void setInProgressFaults(long inProgressFaults) {
        this.inProgressFaults = inProgressFaults;
    }

    public long getResolvedFaults() {
        return resolvedFaults;
    }

    public void setResolvedFaults(long resolvedFaults) {
        this.resolvedFaults = resolvedFaults;
    }

    public long getClosedFaults() {
        return closedFaults;
    }

    public void setClosedFaults(long closedFaults) {
        this.closedFaults = closedFaults;
    }

    public long getRejectedFaults() {
        return rejectedFaults;
    }

    public void setRejectedFaults(long rejectedFaults) {
        this.rejectedFaults = rejectedFaults;
    }

    public long getHighPriorityFaults() {
        return highPriorityFaults;
    }

    public void setHighPriorityFaults(long highPriorityFaults) {
        this.highPriorityFaults = highPriorityFaults;
    }

    public long getCriticalPriorityFaults() {
        return criticalPriorityFaults;
    }

    public void setCriticalPriorityFaults(long criticalPriorityFaults) {
        this.criticalPriorityFaults = criticalPriorityFaults;
    }
}
