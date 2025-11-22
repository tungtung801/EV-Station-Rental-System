package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "BookingId")
  Long bookingId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VehicleId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Vehicle vehicle;

  @Column(name = "BookingType", nullable = false)
  @Enumerated(EnumType.STRING)
  BookingTypeEnum bookingType; // ONLINE / OFFLINE

  @Column(name = "StartTime", nullable = false)
  LocalDateTime startTime;

  @Column(name = "EndTime", nullable = false)
  LocalDateTime endTime;

  @Column(name = "TotalAmount", nullable = false, precision = 19, scale = 4)
  BigDecimal totalAmount;

  @Column(name = "Status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  BookingStatusEnum status = BookingStatusEnum.PENDING; // Mặc định chờ

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;


    @Column(name = "DiscountCode", length = 50)
    String discountCode; // Lưu mã (VD: SALE50)

    @Column(name = "DiscountAmount", precision = 19, scale = 4)
    BigDecimal discountAmount; // Số tiền được giảm

  @OneToOne(
      mappedBy = "booking",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Rental rental;
}
