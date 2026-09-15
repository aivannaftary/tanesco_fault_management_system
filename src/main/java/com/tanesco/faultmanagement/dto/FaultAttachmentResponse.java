package com.tanesco.faultmanagement.dto;

import java.time.LocalDateTime;

public class FaultAttachmentResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

    public FaultAttachmentResponse(
            Long id,
            String originalFileName,
            String contentType,
            long fileSize,
            String uploadedBy,
            LocalDateTime uploadedAt
    ) {
        this.id = id;
        this.originalFileName =
                originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
