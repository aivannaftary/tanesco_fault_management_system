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

        Fault fault = findFault(referenceNumber);

        User technician = userRepository.findById(
                        request.getTechnicianId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Technician was not found."
                        )
                );

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new IllegalArgumentException(
                    "Selected user is not a technician."
            );
        }

        User updatedBy = findUser(updatedByUsername);

        fault.setTechnician(technician);
        fault.setStatus(FaultStatus.ASSIGNED);
        fault.setAssignedAt(LocalDateTime.now());

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

        faultUpdateRepository.save(update);

        return convertToResponse(savedFault);
    }

    @Transactional
    public FaultResponse updateStatus(
            String referenceNumber,
            UpdateFaultStatusRequest request,
            String updatedByUsername
    ) {

        Fault fault = findFault(referenceNumber);

        User updatedBy =
                findUser(updatedByUsername);

        FaultStatus newStatus =
                request.getStatus();

        fault.setStatus(newStatus);

        if (newStatus == FaultStatus.IN_PROGRESS
                && fault.getStartedAt() == null) {

            fault.setStartedAt(
                    LocalDateTime.now()
            );
        }

        if (newStatus == FaultStatus.CLOSED
                && fault.getClosedAt() == null) {

            fault.setClosedAt(
                    LocalDateTime.now()
            );
        }

        Fault savedFault =
                faultRepository.save(fault);

        FaultUpdate update =
                new FaultUpdate(
                        savedFault,
                        updatedBy,
                        newStatus,
                        request.getMessage().trim()
                );

        faultUpdateRepository.save(update);

        return convertToResponse(savedFault);
    }

    @Transactional
    public FaultResponse resolveFault(
            String referenceNumber,
            ResolveFaultRequest request,
            String updatedByUsername
    ) {

        Fault fault = findFault(referenceNumber);

        User updatedBy =
                findUser(updatedByUsername);

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
                        updatedBy,
                        FaultStatus.RESOLVED,
                        "Fault has been resolved. "
                                + request.getResolutionNotes().trim()
                );

        faultUpdateRepository.save(update);

        return convertToResponse(savedFault);
    }

    private Fault findFault(
            String referenceNumber
    ) {

        return faultRepository
                .findByReferenceNumber(referenceNumber)
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
                                "User account was not found."
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