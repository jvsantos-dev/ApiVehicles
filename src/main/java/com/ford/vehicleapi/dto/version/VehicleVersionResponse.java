package com.ford.vehicleapi.dto.version;

import com.ford.vehicleapi.domain.FuelType;
import com.ford.vehicleapi.domain.VehicleVersion;

import java.math.BigDecimal;

public record VehicleVersionResponse(
        Long id,
        Long modelId,
        String modelName,
        String name,
        Integer modelYear,
        String engine,
        Integer horsepower,
        String transmission,
        FuelType fuelType,
        BigDecimal price
) {

    public static VehicleVersionResponse from(VehicleVersion version) {
        return new VehicleVersionResponse(version.getId(), version.getModel().getId(), version.getModel().getName(),
                version.getName(), version.getModelYear(), version.getEngine(), version.getHorsepower(),
                version.getTransmission(), version.getFuelType(), version.getPrice());
    }
}
