package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultUpdateResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaultHistoryService {

    private final FaultRepository faultRepository;

    private final FaultUpdateRepository faultUpdateRepository;

    public FaultHistoryService(
            FaultRepository faultRepository,
            FaultUpdateRepository faultUpdateRepository
    ) {
        this.faultRepository = faultRepository;
        this.faultUpdateRepository = faultUpdateRepository;
    }

    @Transactional(readOnly = true)
    public List<FaultUpdateResponse> getFaultHistory(
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

        List<FaultUpdate> updates =
                faultUpdateRepository
                        .findByFaultIdOrderByCreatedAtAsc(
                                fault.getId()
                        );

        return updates
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private FaultUpdateResponse convertToResponse(
            FaultUpdate faultUpdate
    ) {

        User updatedBy =
                faultUpdate.getUpdatedBy();

        Long updatedByUserId = null;
        String updatedByUsername = null;
        String updatedByFullName = null;

        if (updatedBy != null) {

            updatedByUserId =
                    updatedBy.getId();

            updatedByUsername =
                    updatedBy.getUsername();

            updatedByFullName =
                    updatedBy.getFullName();
        }

        return new FaultUpdateResponse(
                faultUpdate.getId(),
                faultUpdate.getStatus(),
                faultUpdate.getMessage(),
                updatedByUserId,
                updatedByUsername,
                updatedByFullName,
                faultUpdate.getCreatedAt()
        );
    }
}
