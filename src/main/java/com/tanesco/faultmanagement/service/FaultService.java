package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultRequest;
import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FaultService {

    private final FaultRepository faultRepository;

    private final FaultUpdateRepository faultUpdateRepository;

    private final UserRepository userRepository;

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

        User customer = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer account was not found."
                        )
                );

        Fault fault = new Fault();

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

        if (request.getArea() != null
                && !request.getArea().isBlank()) {

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

        FaultUpdate firstUpdate =
                new FaultUpdate(
                        savedFault,
                        customer,
                        FaultStatus.SUBMITTED,
                        "Fault reported successfully and is waiting for review."
                );

        faultUpdateRepository.save(firstUpdate);

        return convertToResponse(savedFault);
    }

    @Transactional(readOnly = true)
    public List<FaultResponse> getCustomerFaults(
            String username
    ) {

        User customer = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer account was not found."
                        )
                );

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
            String referenceNumber
    ) {

        Fault fault =
                faultRepository
                        .findByReferenceNumber(referenceNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fault was not found with reference number: "
                                                + referenceNumber
                                )
                        );

        return convertToResponse(fault);
    }

    private String generateReferenceNumber() {

        String year =
                String.valueOf(
                        LocalDateTime.now().getYear()
                );

        String date =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "MMdd"
                                )
                        );

        String referenceNumber;

        do {

            int randomNumber =
                    ThreadLocalRandom.current()
                            .nextInt(
                                    1000,
                                    10000
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