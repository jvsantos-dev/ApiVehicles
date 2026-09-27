package com.ford.vehicleapi.dto.model;

import com.ford.vehicleapi.domain.VehicleCategory;
import com.ford.vehicleapi.domain.VehicleModel;
import com.ford.vehicleapi.domain.VehicleVersion;

import java.math.BigDecimal;
import java.util.Comparator;

public record VehicleModelResponse(
        Long id,
        String name,
        VehicleCategory category,
        String description,
        Integer launchYear,
        boolean active,
        int versionsCount,
        BigDecimal startingPrice
) {

    public static VehicleModelResponse from(VehicleModel model) {
        BigDecimal startingPrice = model.getVersions().stream()
                .map(VehicleVersion::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new VehicleModelResponse(model.getId(), model.getName(), model.getCategory(), model.getDescription(),
                model.getLaunchYear(), model.isActive(), model.getVersions().size(), startingPrice);
    }
}
