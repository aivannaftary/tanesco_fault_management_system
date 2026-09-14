package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.FaultUpdateResponse;
import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultUpdate;
import com.tanesco.faultmanagement.entity.Role;
import com.tanesco.faultmanagement.entity.User;
import com.tanesco.faultmanagement.repository.FaultRepository;
import com.tanesco.faultmanagement.repository.FaultUpdateRepository;
import com.tanesco.faultmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaultHistoryService {

    private final FaultRepository faultRepository;
    private final FaultUpdateRepository faultUpdateRepository;
    private final UserRepository userRepository;

    public FaultHistoryService(
            FaultRepository faultRepository,
            FaultUpdateRepository faultUpdateRepository,
            UserRepository userRepository
    ) {
        this.faultRepository = faultRepository;
        this.faultUpdateRepository = faultUpdateRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FaultUpdateResponse> getFaultHistory(
            String referenceNumber,
            String username
    ) {

        Fault fault =
                faultRepository
                        .findByReferenceNumber(
                                referenceNumber
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fault was not found with reference number: "
                                                + referenceNumber
                                )
                        );

        User currentUser =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User was not found: "
                                                + username
                                )
                        );

        if (currentUser.getRole() == Role.CUSTOMER) {

            if (!fault.getCustomer()
                    .getId()
                    .equals(currentUser.getId())) {

                throw new AccessDeniedException(
                        "You are not allowed to view the history of this fault."
                );
            }
        }

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