package com.tanesco.faultmanagement.repository;

import com.tanesco.faultmanagement.entity.Fault;
import com.tanesco.faultmanagement.entity.FaultStatus;
import com.tanesco.faultmanagement.entity.Priority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaultRepository extends JpaRepository<Fault, Long> {

    Optional<Fault> findByReferenceNumber(String referenceNumber);

    List<Fault> findByCustomerIdOrderByReportedAtDesc(Long customerId);

    List<Fault> findByTechnicianIdOrderByReportedAtDesc(Long technicianId);

    List<Fault> findByStatusOrderByReportedAtDesc(FaultStatus status);

    List<Fault> findByPriorityOrderByReportedAtDesc(Priority priority);

    List<Fault> findByCategoryIgnoreCaseOrderByReportedAtDesc(String category);

    List<Fault> findByLocationContainingIgnoreCaseOrderByReportedAtDesc(String location);

    long countByStatus(FaultStatus status);

    long countByPriority(Priority priority);

    boolean existsByReferenceNumber(String referenceNumber);
}