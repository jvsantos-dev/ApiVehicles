package com.ford.vehicleapi.dto.testdrive;

import com.ford.vehicleapi.domain.TestDrive;
import com.ford.vehicleapi.domain.TestDriveStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record TestDriveResponse(
        Long id,
        Long customerId,
        String customerName,
        Long versionId,
        String vehicle,
        LocalDateTime scheduledAt,
        String dealership,
        String notes,
        TestDriveStatus status,
        Instant createdAt
) {

    public static TestDriveResponse from(TestDrive testDrive) {
        var version = testDrive.getVersion();
        String vehicle = "Ford %s %s %d".formatted(version.getModel().getName(), version.getName(), version.getModelYear());
        return new TestDriveResponse(testDrive.getId(), testDrive.getCustomer().getId(), testDrive.getCustomer().getName(),
                version.getId(), vehicle, testDrive.getScheduledAt(), testDrive.getDealership(), testDrive.getNotes(),
                testDrive.getStatus(), testDrive.getCreatedAt());
    }
}
