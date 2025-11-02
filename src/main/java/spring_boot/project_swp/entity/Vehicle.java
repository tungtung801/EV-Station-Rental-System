package spring_boot.project_swp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
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
  private Long vehicleId;

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

  @Column(name = "ImageUrl", length = 255)
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ModelId", nullable = false)
  private VehicleModel vehicleModel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "StationId", nullable = false)
  @JsonIgnore
  private Station station;

  public Vehicle(Long vehicleId) {
    this.vehicleId = vehicleId;
  }

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Rental> rentals;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<IncidentReports> incidentReports;
}
