package com.tanesco.faultmanagement.service;

import com.tanesco.faultmanagement.dto.DashboardResponse;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;
import com.tanesco.faultmanagement.repository.FaultRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final FaultRepository faultRepository;

    public DashboardService(
            FaultRepository faultRepository
    ) {
        this.faultRepository = faultRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStatistics() {

        long totalFaults =
                faultRepository.count();

        long submitted =
                faultRepository.countByStatus(
                        FaultStatus.SUBMITTED
                );

        long underReview =
                faultRepository.countByStatus(
                        FaultStatus.UNDER_REVIEW
                );

        long assigned =
                faultRepository.countByStatus(
                        FaultStatus.ASSIGNED
                );

        long inProgress =
                faultRepository.countByStatus(
                        FaultStatus.IN_PROGRESS
                );

        long resolved =
                faultRepository.countByStatus(
                        FaultStatus.RESOLVED
                );

        long closed =
                faultRepository.countByStatus(
                        FaultStatus.CLOSED
                );

        long rejected =
                faultRepository.countByStatus(
                        FaultStatus.REJECTED
                );

        long highPriority =
                faultRepository.countByPriority(
                        Priority.HIGH
                );

        long criticalPriority =
                faultRepository.countByPriority(
                        Priority.CRITICAL
                );

        return new DashboardResponse(
                totalFaults,
                submitted,
                underReview,
                assigned,
                inProgress,
                resolved,
                closed,
                rejected,
                highPriority,
                criticalPriority
        );
    }
}