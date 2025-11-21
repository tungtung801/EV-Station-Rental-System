package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*; // Dùng *
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
  @EqualsAndHashCode.Exclude // Thêm cái này
  Booking booking;

  // Giữ lại UserId, VehicleId để query nhanh (De-normalization)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User renter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VehicleId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Vehicle vehicle;

  // Trạm giao/nhận
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PickupStationId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Station pickupStation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReturnStationId")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Station returnStation;

  // Nhân viên giao/nhận
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PickupStaffId")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User pickupStaff;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReturnStaffId")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User returnStaff;

  @Column(name = "StartActual")
  LocalDateTime startActual; // Giờ nhận xe thực tế

  @Column(name = "EndActual")
  LocalDateTime endActual; // Giờ trả xe thực tế

  @Column(name = "Total", precision = 19, scale = 4)
  BigDecimal total;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", length = 50)
  @Builder.Default
  RentalStatusEnum status = RentalStatusEnum.PENDING_PICKUP;

  @Column(name = "ContractUrl", length = 500) // Tăng length lên
  String contractUrl;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  // List Payment (Bỏ JsonIgnore đi nhé)
  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  List<Payment> payments;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  List<RentalDiscounts> rentalDiscounts;
}
