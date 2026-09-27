package com.ford.vehicleapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;

/** Agendamento de test drive de uma versão, feito por um cliente. */
@Entity
@Table(name = "test_drives")
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private VehicleVersion version;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, length = 120)
    private String dealership;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestDriveStatus status = TestDriveStatus.REQUESTED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TestDrive() {
    }

    public TestDrive(User customer, VehicleVersion version, LocalDateTime scheduledAt, String dealership, String notes) {
        this.customer = customer;
        this.version = version;
        this.scheduledAt = scheduledAt;
        this.dealership = dealership;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isOwnedBy(Long userId) {
        return customer.getId().equals(userId);
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public VehicleVersion getVersion() { return version; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getDealership() { return dealership; }
    public String getNotes() { return notes; }
    public TestDriveStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(TestDriveStatus status) { this.status = status; }
}
