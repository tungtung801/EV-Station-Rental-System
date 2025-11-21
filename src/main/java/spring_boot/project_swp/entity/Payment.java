package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
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
  @JoinColumn(name = "BookingId") // Nullable (nếu thanh toán phạt lẻ)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Booking booking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RentalId") // Nullable (nếu thanh toán trước khi nhận xe)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
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
  @Builder.Default
  PaymentStatusEnum status = PaymentStatusEnum.PENDING; // Mặc định Pending

  // Người xác nhận thanh toán (Staff)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "StaffId")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User confirmedBy;

  @Column(name = "Note", length = 255)
  String note;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @Column(name = "ConfirmedAt")
  LocalDateTime confirmedAt;

  @Column(name = "TransactionCode", length = 150) // Mã GD VNPay
  String transactionCode;

  // Người trả tiền
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User payer;
}
