package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultAttachmentResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultAttachment;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultAttachmentRepository;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FaultAttachmentService {

    /*
     * Maximum allowed file size: 10 MB.
     */
    private static final long MAX_FILE_SIZE =
            10 * 1024 * 1024;

    /*
     * File types allowed as fault evidence.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "application/pdf"
            );

    private final FaultRepository faultRepository;
    private final UserRepository userRepository;
    private final FaultAttachmentRepository attachmentRepository;

    /*
     * Evidence files will be stored inside:
     *
     * project-folder/uploads/faults/
     */
    private final Path uploadDirectory =
            Paths.get(
                    "uploads",
                    "faults"
            );

    public FaultAttachmentService(
            FaultRepository faultRepository,
            UserRepository userRepository,
            FaultAttachmentRepository attachmentRepository
    ) {
        this.faultRepository = faultRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
    }

    /*
     * =========================================================
     * UPLOAD FAULT ATTACHMENT
     * =========================================================
     */
    @Transactional
    public FaultAttachmentResponse upload(
            String referenceNumber,
            String username,
            MultipartFile file
    ) {

        /*
         * Check that a file was actually supplied.
         */
        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "A file is required."
            );
        }

        /*
         * Check maximum file size.
         */
        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "File must not exceed 10 MB."
            );
        }

        /*
         * Check file content type.
         */
        String contentType =
                file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        contentType
                )) {

            throw new IllegalArgumentException(
                    "Only JPEG, PNG, WEBP and PDF files are allowed."
            );
        }

        /*
         * Find the fault using its reference number.
         */
        Fault fault =
                faultRepository
                        .findByReferenceNumber(
                                referenceNumber
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fault was not found."
                                )
                        );

        /*
         * Find the currently authenticated user.
         */
        User user =
                userRepository
                        .findByUsername(
                                username
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User was not found."
                                )
                        );

        /*
         * Check whether this user is allowed
         * to access this fault's evidence.
         */
        verifyAccess(
                fault,
                user
        );

        try {

            /*
             * Convert the upload directory to an
             * absolute normalized path.
             * Example upload directory:
             *
             * C:/Users/user/Documents/PROJECTS/
             * tanesco-fault-management-system/
             * uploads/faults/
             */
            Path absoluteDirectory =
                    uploadDirectory
                            .toAbsolutePath()
                            .normalize();

            /*
             * Create uploads/faults if it does
             * not already exist.
             */
            Files.createDirectories(
                    absoluteDirectory
            );

            /*
             * Get the original extension.
             *
             * Example:
             *
             * fault.jpg -> .jpg
             */
            String extension =
                    getExtension(
                            file.getOriginalFilename()
                    );

            /*
             * Generate a unique stored filename
             * so uploaded files cannot overwrite
             * one another.
             */
            String storedName =
                    UUID.randomUUID()
                            + extension;

            /*
             * Build the complete destination path.
             */
            Path absoluteTarget =
                    absoluteDirectory
                            .resolve(
                                    storedName
                            )
                            .normalize();

            /*
             * SECURITY CHECK
             *
             * Make sure the final destination
             * remains inside uploads/faults.
             *
             * This protects against path traversal.
             */
            if (!absoluteTarget.startsWith(
                    absoluteDirectory
            )) {

                throw new IllegalArgumentException(
                        "Invalid file path."
                );
            }

            /*
             * Copy the uploaded file to
             * uploads/faults.
             */
            Files.copy(
                    file.getInputStream(),
                    absoluteTarget,
                    StandardCopyOption.REPLACE_EXISTING
            );

            /*
             * Create database metadata record.
             */
            FaultAttachment attachment =
                    new FaultAttachment();

            attachment.setFault(
                    fault
            );

            attachment.setUploadedBy(
                    user
            );

            attachment.setOriginalFileName(
                    safeOriginalName(
                            file.getOriginalFilename()
                    )
            );

            attachment.setStoredFileName(
                    storedName
            );

            attachment.setContentType(
                    contentType
            );

            attachment.setFileSize(
                    file.getSize()
            );

            /*
             * Save attachment information
             * into MySQL.
             */
            FaultAttachment saved =
                    attachmentRepository
                            .save(
                                    attachment
                            );

            /*
             * Return safe metadata to the client.
             */
            return toResponse(
                    saved
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "The evidence file could not be stored."
            );
        }
    }

    /*
     * =========================================================
     * GET ATTACHMENTS FOR A FAULT
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<FaultAttachmentResponse> getAttachments(
            String referenceNumber,
            String username
    ) {

        /*
         * Find fault.
         */
        Fault fault =
                faultRepository
                        .findByReferenceNumber(
                                referenceNumber
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fault was not found."
                                )
                        );

        /*
         * Find authenticated user.
         */
        User user =
                userRepository
                        .findByUsername(
                                username
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User was not found."
                                )
                        );

        /*
         * Check access permission.
         */
        verifyAccess(
                fault,
                user
        );

        /*
         * Return attachment metadata.
         */
        return attachmentRepository
                .findByFaultIdOrderByUploadedAtAsc(
                        fault.getId()
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    /*
     * =========================================================
     * ACCESS CONTROL
     * =========================================================
     */
    private void verifyAccess(
            Fault fault,
            User user
    ) {

        /*
         * CUSTOMER
         *
         * Customers can only access evidence
         * belonging to their own faults.
         */
        if (user.getRole() == Role.CUSTOMER) {

            if (!fault.getCustomer()
                    .getId()
                    .equals(
                            user.getId()
                    )) {

                throw new AccessDeniedException(
                        "You cannot access evidence for another customer's fault."
                );
            }

            return;
        }

        /*
         * TECHNICIAN
         *
         * Technicians can only access evidence
         * for faults assigned to them.
         */
        if (user.getRole() == Role.TECHNICIAN) {

            if (fault.getTechnician() == null
                    || !fault.getTechnician()
                    .getId()
                    .equals(
                            user.getId()
                    )) {

                throw new AccessDeniedException(
                        "You can only access evidence for faults assigned to you."
                );
            }
        }

        /*
         * OFFICER and ADMIN are allowed
         * to access fault evidence.
         */
    }

    /*
     * =========================================================
     * SAFE ORIGINAL FILE NAME
     * =========================================================
     */
    private String safeOriginalName(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            return "file";
        }

        /*
         * Remove any directory information
         * from the supplied filename.
         */
        return Paths.get(
                        name
                )
                .getFileName()
                .toString();
    }

    /*
     * =========================================================
     * GET FILE EXTENSION
     * =========================================================
     */
    private String getExtension(
            String fileName
    ) {

        if (fileName == null
                || fileName.isBlank()) {

            return "";
        }

        int index =
                fileName.lastIndexOf('.');

        if (index < 0) {

            return "";
        }

        return fileName
                .substring(
                        index
                )
                .toLowerCase();
    }

    /*
     * =========================================================
     * CONVERT ENTITY TO RESPONSE DTO
     * =========================================================
     */
    private FaultAttachmentResponse toResponse(
            FaultAttachment attachment
    ) {

        return new FaultAttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedBy()
                        .getUsername(),
                attachment.getUploadedAt()
        );
    }
}