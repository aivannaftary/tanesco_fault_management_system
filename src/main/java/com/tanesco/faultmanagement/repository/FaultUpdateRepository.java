package com.tanesco.faultmanagement.repository;

import com.tanesco.faultmanagement.entity.FaultUpdate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultUpdateRepository extends JpaRepository<FaultUpdate, Long> {

    List<FaultUpdate> findByFaultIdOrderByCreatedAtAsc(Long faultId);

    List<FaultUpdate> findByFaultReferenceNumberOrderByCreatedAtAsc(String referenceNumber);
}
