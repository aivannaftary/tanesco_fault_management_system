package com.tanesco.faultmanagement.repository;

import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FaultRepository
        extends JpaRepository<Fault, Long>,
        JpaSpecificationExecutor<Fault> {

    Optional<Fault> findByReferenceNumber(
            String referenceNumber
    );

    boolean existsByReferenceNumber(
            String referenceNumber
    );

    List<Fault> findByCustomerIdOrderByReportedAtDesc(
            Long customerId
    );

    List<Fault> findByStatusOrderByReportedAtDesc(
            FaultStatus status
    );

    long countByStatus(
            FaultStatus status
    );

    long countByPriority(
            Priority priority
    );
}