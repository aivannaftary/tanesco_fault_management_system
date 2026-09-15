package com.tanesco.faultmanagement.repository;

import com.tanesco.faultmanagement.entity.FaultAttachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaultAttachmentRepository
        extends JpaRepository<FaultAttachment, Long> {

    List<FaultAttachment>
    findByFaultIdOrderByUploadedAtAsc(
            Long faultId
    );
}