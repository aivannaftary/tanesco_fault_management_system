package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaultSearchService {

    private final FaultRepository faultRepository;

    public FaultSearchService(
            FaultRepository faultRepository
    ) {
        this.faultRepository = faultRepository;
    }

    @Transactional(readOnly = true)
    public List<FaultResponse> search(
            String referenceNumber,
            FaultStatus status,
            Priority priority,
            String category,
            String area,
            String location,
            Long technicianId
    ) {

        Specification<Fault> specification =
                (root, query, cb) ->
                        cb.conjunction();

        if (referenceNumber != null
                && !referenceNumber.isBlank()) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.like(
                                            cb.lower(
                                                    root.get(
                                                            "referenceNumber"
                                                    )
                                            ),
                                            "%"
                                                    + referenceNumber
                                                    .toLowerCase()
                                                    + "%"
                                    )
                    );
        }

        if (status != null) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.equal(
                                            root.get("status"),
                                            status
                                    )
                    );
        }

        if (priority != null) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.equal(
                                            root.get("priority"),
                                            priority
                                    )
                    );
        }

        if (category != null
                && !category.isBlank()) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.like(
                                            cb.lower(
                                                    root.get("category")
                                            ),
                                            "%"
                                                    + category
                                                    .toLowerCase()
                                                    + "%"
                                    )
                    );
        }

        if (area != null
                && !area.isBlank()) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.like(
                                            cb.lower(
                                                    root.get("area")
                                            ),
                                            "%"
                                                    + area
                                                    .toLowerCase()
                                                    + "%"
                                    )
                    );
        }

        if (location != null
                && !location.isBlank()) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.like(
                                            cb.lower(
                                                    root.get("location")
                                            ),
                                            "%"
                                                    + location
                                                    .toLowerCase()
                                                    + "%"
                                    )
                    );
        }

        if (technicianId != null) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.equal(
                                            root.get("technician")
                                                    .get("id"),
                                            technicianId
                                    )
                    );
        }

        return faultRepository
                .findAll(specification)
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