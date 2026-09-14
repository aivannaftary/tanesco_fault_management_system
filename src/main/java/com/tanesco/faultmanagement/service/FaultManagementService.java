package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.AssignFaultRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.dto.ResolveFaultRequest;
import com.tanesco.faultmanagement.dto.UpdateFaultStatusRequest;

import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;

import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FaultManagementService {

    private final FaultRepository faultRepository;
    private final FaultUpdateRepository faultUpdateRepository;
    private final UserRepository userRepository;

    public FaultManagementService(
            FaultRepository faultRepository,
            FaultUpdateRepository faultUpdateRepository,
            UserRepository userRepository
    ) {
        this.faultRepository = faultRepository;
        this.faultUpdateRepository = faultUpdateRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FaultResponse assignFault(
            String referenceNumber,
            AssignFaultRequest request,
            String updatedByUsername
    ) {

        Fault fault =
                findFault(referenceNumber);

        User updatedBy =
                findUser(updatedByUsername);

        /*
         * Only an OFFICER or ADMIN can assign faults.
         */
        if (
                updatedBy.getRole() != Role.OFFICER
                        &&
                updatedBy.getRole() != Role.ADMIN
        ) {

            throw new AccessDeniedException(
                    "Only an officer or administrator can assign faults."
            );
        }

        /*
         * Fault must still be waiting for assignment.
         */
        if (
                fault.getStatus() != FaultStatus.SUBMITTED
                        &&
                fault.getStatus() != FaultStatus.UNDER_REVIEW
        ) {

            throw new IllegalStateException(
                    "Only submitted or under-review faults can be assigned."
            );
        }

        User technician =
                userRepository
                        .findById(
                                request.getTechnicianId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Technician was not found with ID: "
                                                + request.getTechnicianId()
                                )
                        );

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new IllegalArgumentException(
                    "The selected user is not a technician."
            );
        }

        fault.setTechnician(
                technician
        );

        fault.setStatus(
                FaultStatus.ASSIGNED
        );

        fault.setAssignedAt(
                LocalDateTime.now()
        );

        Fault savedFault =
                faultRepository.save(fault);

        FaultUpdate update =
                new FaultUpdate(
                        savedFault,
                        updatedBy,
                        FaultStatus.ASSIGNED,
                        "Fault assigned to technician "
                                + technician.getFullName()
                                + "."
                );

        faultUpdateRepository.save(
                update
        );

        return convertToResponse(
                savedFault
        );
    }

    @Transactional
    public FaultResponse updateStatus(
            String referenceNumber,
            UpdateFaultStatusRequest request,
            String updatedByUsername
    ) {

        Fault fault =
                findFault(referenceNumber);

        User updatedBy =
                findUser(updatedByUsername);

        FaultStatus currentStatus =
                fault.getStatus();

        FaultStatus requestedStatus =
                request.getStatus();

        /*
         * TECHNICIAN WORKFLOW
         */
        if (updatedBy.getRole() == Role.TECHNICIAN) {

            validateAssignedTechnician(
                    fault,
                    updatedBy
            );

            if (
                    currentStatus == FaultStatus.ASSIGNED
                            &&
                    requestedStatus == FaultStatus.IN_PROGRESS
            ) {

                fault.setStatus(
                        FaultStatus.IN_PROGRESS
                );

                if (fault.getStartedAt() == null) {

                    fault.setStartedAt(
                            LocalDateTime.now()
                    );
                }

            } else {

                throw new IllegalStateException(
                        "A technician can only change an assigned fault to IN_PROGRESS."
                );
            }
        }

        /*
         * OFFICER / ADMIN WORKFLOW
         */
        else if (
                updatedBy.getRole() == Role.OFFICER
                        ||
                updatedBy.getRole() == Role.ADMIN
        ) {

            validateOfficerTransition(
                    currentStatus,
                    requestedStatus
            );

            fault.setStatus(
                    requestedStatus
            );

            if (requestedStatus == FaultStatus.CLOSED) {

                fault.setClosedAt(
                        LocalDateTime.now()
                );
            }
        }

        else {

            throw new AccessDeniedException(
                    "You are not allowed to update fault status."
            );
        }

        Fault savedFault =
                faultRepository.save(fault);

        FaultUpdate update =
                new FaultUpdate(
                        savedFault,
                        updatedBy,
                        requestedStatus,
                        request.getMessage().trim()
                );

        faultUpdateRepository.save(
                update
        );

        return convertToResponse(
                savedFault
        );
    }

    @Transactional
    public FaultResponse resolveFault(
            String referenceNumber,
            ResolveFaultRequest request,
            String updatedByUsername
    ) {

        Fault fault =
                findFault(referenceNumber);

        User technician =
                findUser(updatedByUsername);

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new AccessDeniedException(
                    "Only a technician can resolve a fault."
            );
        }

        validateAssignedTechnician(
                fault,
                technician
        );

        if (
                fault.getStatus()
                        != FaultStatus.IN_PROGRESS
        ) {

            throw new IllegalStateException(
                    "Only an IN_PROGRESS fault can be resolved."
            );
        }

        fault.setActualCause(
                request.getActualCause().trim()
        );

        fault.setResolutionNotes(
                request.getResolutionNotes().trim()
        );

        fault.setStatus(
                FaultStatus.RESOLVED
        );

        fault.setResolvedAt(
                LocalDateTime.now()
        );

        Fault savedFault =
                faultRepository.save(fault);

        FaultUpdate update =
                new FaultUpdate(
                        savedFault,
                        technician,
                        FaultStatus.RESOLVED,
                        "Fault has been resolved. "
                                + request.getResolutionNotes().trim()
                );

        faultUpdateRepository.save(
                update
        );

        return convertToResponse(
                savedFault
        );
    }

    private void validateAssignedTechnician(
            Fault fault,
            User technician
    ) {

        if (fault.getTechnician() == null) {

            throw new AccessDeniedException(
                    "This fault has not been assigned to a technician."
            );
        }

        if (
                !fault.getTechnician()
                        .getId()
                        .equals(
                                technician.getId()
                        )
        ) {

            throw new AccessDeniedException(
                    "You can only work on faults assigned to you."
            );
        }
    }

    private void validateOfficerTransition(
            FaultStatus currentStatus,
            FaultStatus requestedStatus
    ) {

        boolean validTransition = false;

        /*
         * SUBMITTED -> UNDER_REVIEW
         */
        if (
                currentStatus == FaultStatus.SUBMITTED
                        &&
                requestedStatus == FaultStatus.UNDER_REVIEW
        ) {

            validTransition = true;
        }

        /*
         * SUBMITTED -> REJECTED
         */
        else if (
                currentStatus == FaultStatus.SUBMITTED
                        &&
                requestedStatus == FaultStatus.REJECTED
        ) {

            validTransition = true;
        }

        /*
         * UNDER_REVIEW -> REJECTED
         */
        else if (
                currentStatus == FaultStatus.UNDER_REVIEW
                        &&
                requestedStatus == FaultStatus.REJECTED
        ) {

            validTransition = true;
        }

        /*
         * RESOLVED -> CLOSED
         */
        else if (
                currentStatus == FaultStatus.RESOLVED
                        &&
                requestedStatus == FaultStatus.CLOSED
        ) {

            validTransition = true;
        }

        if (!validTransition) {

            throw new IllegalStateException(
                    "Invalid fault status transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
                            + "."
            );
        }
    }

    private Fault findFault(
            String referenceNumber
    ) {

        return faultRepository
                .findByReferenceNumber(
                        referenceNumber
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Fault was not found with reference number: "
                                        + referenceNumber
                        )
                );
    }

    private User findUser(
            String username
    ) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User was not found: "
                                        + username
                        )
                );
    }

    private FaultResponse convertToResponse(
            Fault fault
    ) {

        User customer =
                fault.getCustomer();

        User technician =
                fault.getTechnician();

        Long technicianId = null;
        String technicianUsername = null;
        String technicianFullName = null;

        if (technician != null) {

            technicianId =
                    technician.getId();

            technicianUsername =
                    technician.getUsername();

            technicianFullName =
                    technician.getFullName();
        }

        return new FaultResponse(
                fault.getId(),
                fault.getReferenceNumber(),

                customer.getId(),
                customer.getUsername(),
                customer.getFullName(),

                technicianId,
                technicianUsername,
                technicianFullName,

                fault.getCategory(),
                fault.getDescription(),
                fault.getLocation(),
                fault.getArea(),

                fault.getPriority(),
                fault.getStatus(),

                fault.getSuspectedCause(),
                fault.getActualCause(),
                fault.getResolutionNotes(),

                fault.getReportedAt(),
                fault.getAssignedAt(),
                fault.getStartedAt(),
                fault.getResolvedAt(),
                fault.getClosedAt(),
                fault.getUpdatedAt()
        );
    }
}