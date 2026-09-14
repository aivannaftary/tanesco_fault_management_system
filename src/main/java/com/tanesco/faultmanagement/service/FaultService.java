package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FaultService {

    private final FaultRepository faultRepository;
    private final FaultUpdateRepository faultUpdateRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public FaultService(
            FaultRepository faultRepository,
            FaultUpdateRepository faultUpdateRepository,
            UserRepository userRepository
    ) {
        this.faultRepository = faultRepository;
        this.faultUpdateRepository = faultUpdateRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FaultResponse createFault(
            String username,
            FaultRequest request
    ) {

        User customer =
                findUserByUsername(username);

        if (customer.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException(
                    "Only customers are allowed to report faults."
            );
        }

        Fault fault =
                new Fault();

        fault.setReferenceNumber(
                generateReferenceNumber()
        );

        fault.setCustomer(customer);

        fault.setCategory(
                request.getCategory().trim()
        );

        fault.setDescription(
                request.getDescription().trim()
        );

        fault.setLocation(
                request.getLocation().trim()
        );

        if (request.getArea() != null) {
            fault.setArea(
                    request.getArea().trim()
            );
        }

        fault.setPriority(
                request.getPriority()
        );

        fault.setStatus(
                FaultStatus.SUBMITTED
        );

        Fault savedFault =
                faultRepository.save(fault);

        FaultUpdate initialUpdate =
                new FaultUpdate(
                        savedFault,
                        customer,
                        FaultStatus.SUBMITTED,
                        "Fault reported successfully and is waiting for review."
                );

        faultUpdateRepository.save(
                initialUpdate
        );

        return convertToResponse(
                savedFault
        );
    }

    @Transactional(readOnly = true)
    public List<FaultResponse> getCustomerFaults(
            String username
    ) {

        User customer =
                findUserByUsername(username);

        if (customer.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException(
                    "Only customers can access the customer fault list."
            );
        }

        return faultRepository
                .findByCustomerIdOrderByReportedAtDesc(
                        customer.getId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FaultResponse getFaultByReferenceNumber(
            String referenceNumber,
            String username
    ) {

        Fault fault =
                findFault(referenceNumber);

        User currentUser =
                findUserByUsername(username);

        if (currentUser.getRole() == Role.CUSTOMER) {

            if (!fault.getCustomer()
                    .getId()
                    .equals(currentUser.getId())) {

                throw new AccessDeniedException(
                        "You are not allowed to view this fault."
                );
            }
        }

        return convertToResponse(
                fault
        );
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

    private User findUserByUsername(
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

    private String generateReferenceNumber() {

        String year =
                String.valueOf(
                        LocalDate.now().getYear()
                );

        String date =
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern(
                                "MMdd"
                        )
                );

        String referenceNumber;

        do {

            int randomNumber =
                    1000
                            + secureRandom.nextInt(
                            9000
                    );

            referenceNumber =
                    "TAN-"
                            + year
                            + "-"
                            + date
                            + "-"
                            + randomNumber;

        } while (
                faultRepository
                        .existsByReferenceNumber(
                                referenceNumber
                        )
        );

        return referenceNumber;
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