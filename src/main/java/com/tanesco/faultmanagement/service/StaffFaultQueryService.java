package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffFaultQueryService {

    private final FaultRepository faultRepository;

    public StaffFaultQueryService(
            FaultRepository faultRepository
    ) {
        this.faultRepository = faultRepository;
    }

    @Transactional(readOnly = true)
    public List<FaultResponse> getAllFaults() {

        return faultRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaultResponse> getFaultsByStatus(
            FaultStatus status
    ) {

        return faultRepository
                .findByStatusOrderByReportedAtDesc(status)
                .stream()
                .map(this::convertToResponse)
                .toList();
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
