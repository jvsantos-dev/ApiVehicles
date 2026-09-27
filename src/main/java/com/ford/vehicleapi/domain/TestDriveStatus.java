package com.ford.vehicleapi.domain;

import java.util.EnumSet;
import java.util.Set;

public enum TestDriveStatus {
    REQUESTED,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    /** Transições de status permitidas a partir do status atual. */
    public Set<TestDriveStatus> allowedTransitions() {
        return switch (this) {
            case REQUESTED -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(TestDriveStatus.class);
        };
    }

    public boolean canTransitionTo(TestDriveStatus target) {
        return allowedTransitions().contains(target);
    }
}
