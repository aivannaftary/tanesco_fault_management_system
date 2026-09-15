package com.tanesco.faultmanagement.controller;

import com.tanesco.faultmanagement.dto.FaultAttachmentResponse;
import com.tanesco.faultmanagement.service.FaultAttachmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/faults")
public class FaultAttachmentController {

    private final FaultAttachmentService service;

    public FaultAttachmentController(
            FaultAttachmentService service
    ) {
        this.service = service;
    }

    @PostMapping(
            value = "/{referenceNumber}/attachments",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<FaultAttachmentResponse>
    upload(
            @PathVariable String referenceNumber,
            @RequestPart("file")
            MultipartFile file,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.upload(
                                referenceNumber,
                                authentication.getName(),
                                file
                        )
                );
    }

    @GetMapping(
            "/{referenceNumber}/attachments"
    )
    public ResponseEntity<
            List<FaultAttachmentResponse>>
    getAttachments(
            @PathVariable String referenceNumber,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.getAttachments(
                        referenceNumber,
                        authentication.getName()
                )
        );
    }
}
