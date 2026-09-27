package com.ford.vehicleapi.repository;

import com.ford.vehicleapi.domain.VehicleCategory;
import com.ford.vehicleapi.domain.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findByCategoryOrderByNameAsc(VehicleCategory category);

    List<VehicleModel> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
