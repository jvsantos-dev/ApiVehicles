package com.ford.vehicleapi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Modelo de veículo da linha Ford (ex.: Ranger, Mustang, Bronco Sport). */
@Entity
@Table(name = "vehicle_models")
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleCategory category;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer launchYear;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleVersion> versions = new ArrayList<>();

    protected VehicleModel() {
    }

    public VehicleModel(String name, VehicleCategory category, String description, Integer launchYear, boolean active) {
        update(name, category, description, launchYear, active);
    }

    public void update(String name, VehicleCategory category, String description, Integer launchYear, boolean active) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.launchYear = launchYear;
        this.active = active;
    }

    public VehicleVersion addVersion(VehicleVersion version) {
        version.setModel(this);
        versions.add(version);
        return version;
    }

    public void removeVersion(VehicleVersion version) {
        versions.remove(version);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public VehicleCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public Integer getLaunchYear() { return launchYear; }
    public boolean isActive() { return active; }
    public List<VehicleVersion> getVersions() { return versions; }
}
