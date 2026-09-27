package com.ford.vehicleapi.repository;

import com.ford.vehicleapi.domain.VehicleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleVersionRepository extends JpaRepository<VehicleVersion, Long> {

    List<VehicleVersion> findByModelIdOrderByPriceAsc(Long modelId);

    Optional<VehicleVersion> findByIdAndModelId(Long id, Long modelId);

    boolean existsByModelIdAndNameIgnoreCaseAndModelYear(Long modelId, String name, Integer modelYear);

    boolean existsByModelIdAndNameIgnoreCaseAndModelYearAndIdNot(Long modelId, String name, Integer modelYear, Long id);
}
