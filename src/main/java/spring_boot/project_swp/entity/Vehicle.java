package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private int vehicleId;
    @Column(name = "LicensePlate", nullable = false, unique = true, length = 20)
    private String licensePlate;
    @Column(name = "BatteryCapacity", nullable = false, length = 100)
    private int batteryCapacity;
    @Column(name = "CurrentBatteryLevel", nullable = false, length = 100)
    private int currentBattery;
    @Column(name = "VehicleStatus", nullable = false)
    private String vehicleStatus;
    @Column(name = "PricePerHour", nullable = false)
    private double pricePerHour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", nullable = false)
    private VehicleModel vehicleModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StationId", nullable = false)
    private Station station;
}
