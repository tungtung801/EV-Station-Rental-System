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
@Table(name = "Payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "PaymentId")
  Long paymentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "BookingId")
  Booking booking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RentalId")
  Rental rental;

  @Column(name = "Amount", precision = 12, scale = 2, nullable = false)
  BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "PaymentType", length = 20, nullable = false)
  PaymentTypeEnum paymentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "PaymentMethod", length = 20, nullable = false)
  PaymentMethodEnum paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", length = 20, nullable = false)
  PaymentStatusEnum status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "StaffId")
  @ToString.Exclude
  User confirmedBy;

  @Column(name = "Note", length = 255)
  String note;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @Column(name = "ConfirmedAt")
  LocalDateTime confirmedAt;

  @Column(name = "TransactionCode", length = 150)
  String transactionCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId")
  @ToString.Exclude
  private User payer;
}
