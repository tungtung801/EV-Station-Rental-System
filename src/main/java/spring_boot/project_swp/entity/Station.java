package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Stations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Station {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "StationId")
  Long stationId;

  @Column(name = "StationName", length = 100, columnDefinition = "nvarchar(100)", nullable = false)
  String stationName;

  @Column(name = "Address", length = 255, columnDefinition = "nvarchar(255)", nullable = false)
  String address;

  @Column(name = "Latitude", precision = 10, scale = 2)
  BigDecimal latitude;

  @Column(name = "Longitude", precision = 10, scale = 2)
  BigDecimal longitude;

  @Column(name = "IsActive", nullable = false)
  @Enumerated(EnumType.STRING)
  StationStatusEnum isActive;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "LocationId")
  Location location;

  @OneToMany(mappedBy = "pickupStation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Rental> pickupRentals;

  @OneToMany(mappedBy = "returnStation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Rental> returnRentals;

  @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Vehicle> vehicles;
}
