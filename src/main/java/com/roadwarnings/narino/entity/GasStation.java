package com.roadwarnings.narino.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gas_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GasStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    private String municipality;

    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasGasoline = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasDiesel = true;

    private BigDecimal gasolinePrice;

    private BigDecimal dieselPrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOpen24Hours = false;

    private String openingTime;

    private String closingTime;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}