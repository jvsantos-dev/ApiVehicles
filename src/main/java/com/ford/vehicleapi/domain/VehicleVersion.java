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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/** Versão (trim) de um modelo, ex.: Ranger Limited 3.0 V6 2025. */
@Entity
@Table(name = "vehicle_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"model_id", "name", "model_year"}))
public class VehicleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "model_year", nullable = false)
    private Integer modelYear;

    @Column(nullable = false, length = 80)
    private String engine;

    @Column(nullable = false)
    private Integer horsepower;

    @Column(nullable = false, length = 60)
    private String transmission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuelType fuelType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    protected VehicleVersion() {
    }

    public VehicleVersion(String name, Integer modelYear, String engine, Integer horsepower,
                          String transmission, FuelType fuelType, BigDecimal price) {
        update(name, modelYear, engine, horsepower, transmission, fuelType, price);
    }

    public void update(String name, Integer modelYear, String engine, Integer horsepower,
                       String transmission, FuelType fuelType, BigDecimal price) {
        this.name = name;
        this.modelYear = modelYear;
        this.engine = engine;
        this.horsepower = horsepower;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.price = price;
    }

    void setModel(VehicleModel model) { this.model = model; }

    public Long getId() { return id; }
    public VehicleModel getModel() { return model; }
    public String getName() { return name; }
    public Integer getModelYear() { return modelYear; }
    public String getEngine() { return engine; }
    public Integer getHorsepower() { return horsepower; }
    public String getTransmission() { return transmission; }
    public FuelType getFuelType() { return fuelType; }
    public BigDecimal getPrice() { return price; }
}
