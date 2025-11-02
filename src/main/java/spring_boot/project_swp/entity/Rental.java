package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rental {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "RentalId")
  Long rentalId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "BookingId", nullable = false, unique = true)
  @ToString.Exclude
  Booking booking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RenterId", nullable = false)
  @ToString.Exclude
  User renter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VehicleId", nullable = false)
  @ToString.Exclude
  Vehicle vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PickupStationId", nullable = false)
  @ToString.Exclude
  Station pickupStation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReturnStationId")
  @ToString.Exclude
  Station returnStation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PickupStaffId")
  @ToString.Exclude
  User pickupStaff;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReturnStaffId")
  @ToString.Exclude
  User returnStaff;

  @Column(name = "StartTime", nullable = false)
  LocalDateTime startTime;

  @Column(name = "EndTime")
  LocalDateTime endTime;

  @Column(name = "TotalCost", columnDefinition = "DECIMAL(10,2)")
  Double totalCost;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", length = 50)
  RentalStatusEnum status;

  @Column(name = "ContractUrl", length = 255)
  String contractUrl;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  List<Payment> payments;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  List<RentalDiscounts> rentalDiscounts;
}
