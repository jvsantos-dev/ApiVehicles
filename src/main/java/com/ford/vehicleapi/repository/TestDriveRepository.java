package com.ford.vehicleapi.repository;

import com.ford.vehicleapi.domain.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {

    List<TestDrive> findByCustomerIdOrderByScheduledAtAsc(Long customerId);

    List<TestDrive> findAllByOrderByScheduledAtAsc();

    boolean existsByVersionModelId(Long modelId);

    boolean existsByVersionId(Long versionId);
}
