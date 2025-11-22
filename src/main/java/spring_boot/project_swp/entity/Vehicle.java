package spring_boot.project_swp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Vehicles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VehicleId")
    Long vehicleId;

    @Column(name = "LicensePlate", nullable = false, unique = true, length = 20)
    String licensePlate;

    @Column(name = "CurrentBatteryLevel", nullable = false)
    int currentBattery; // 0-100%

    @Column(name = "VehicleStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    VehicleStatusEnum vehicleStatus = VehicleStatusEnum.AVAILABLE;

    @Column(name = "PricePerHour", nullable = false, precision = 10, scale = 2)
    BigDecimal pricePerHour;


    @Column(name = "ImageUrl", length = 500)
    String imageUrl;

    // --- QUAN HỆ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModelId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    VehicleModel vehicleModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StationId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Station station;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Rental> rentals;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // Tránh vòng lặp vô tận
    List<Booking> bookings;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<IncidentReports> incidentReports;
}
