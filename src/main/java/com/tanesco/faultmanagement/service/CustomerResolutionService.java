package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.ConfirmResolutionRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerResolutionService {

    private final FaultRepository faultRepository;
    private final FaultUpdateRepository faultUpdateRepository;
    private final UserRepository userRepository;

    public CustomerResolutionService(
            FaultRepository faultRepository,
            FaultUpdateRepository faultUpdateRepository,
            UserRepository userRepository
    ) {
        this.faultRepository = faultRepository;
        this.faultUpdateRepository =
                faultUpdateRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FaultResponse confirmResolution(
            String referenceNumber,
            String username,
            ConfirmResolutionRequest request
    ) {

        User customer =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User was not found."
                                )
                        );

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

        if (!fault.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new AccessDeniedException(
                    "You can only confirm your own fault."
            );
        }

        if (fault.getStatus()
                != FaultStatus.RESOLVED) {

            throw new IllegalStateException(
                    "Only a RESOLVED fault can be confirmed."
            );
        }

        fault.setStatus(
                FaultStatus.CLOSED
        );

        fault.setClosedAt(
                LocalDateTime.now()
        );

        Fault saved =
                faultRepository.save(fault);

        faultUpdateRepository.save(
                new FaultUpdate(
                        saved,
                        customer,
                        FaultStatus.CLOSED,
                        request.getMessage().trim()
                )
        );

        return toResponse(saved);
    }

    private FaultResponse toResponse(
            Fault fault
    ) {

        User customer =
                fault.getCustomer();

        User technician =
                fault.getTechnician();

        return new FaultResponse(
                fault.getId(),
                fault.getReferenceNumber(),

                customer.getId(),
                customer.getUsername(),
                customer.getFullName(),

                technician == null
                        ? null
                        : technician.getId(),

                technician == null
                        ? null
                        : technician.getUsername(),

                technician == null
                        ? null
                        : technician.getFullName(),

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
