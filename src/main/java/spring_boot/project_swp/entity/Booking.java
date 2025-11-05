package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VehicleId", nullable = false)
  @ToString.Exclude
  Vehicle vehicle;

  @Column(name = "BookingType", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  BookingTypeEnum bookingType;

  @Column(name = "StartTime", nullable = false)
  LocalDateTime startTime;

  @Column(name = "EndTime", nullable = false)
  LocalDateTime endTime;

  @Column(name = "TotalAmount", nullable = false, columnDefinition = "DECIMAL(19,4)")
  BigDecimal totalAmount;

  @Column(name = "DepositPercent", nullable = false, columnDefinition = "DECIMAL(5,2)")
  BigDecimal depositPercent;

  @Column(name = "ExpectedTotal", nullable = false, columnDefinition = "DECIMAL(19,4)")
  BigDecimal expectedTotal;

  @Column(name = "Status", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  BookingStatusEnum status = BookingStatusEnum.PENDING_DEPOSIT;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @OneToOne(
      mappedBy = "booking",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @ToString.Exclude
  Rental rental;
}
