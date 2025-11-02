package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
  @JoinColumn(name = "RentalId", nullable = false)
  @JsonBackReference
  Rental rental;

  @Column(name = "Amount", nullable = false)
  double amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "PaymentType", nullable = false, length = 50)
  PaymentTypeEnum paymentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "PaymentMethod", nullable = false, length = 50)
  PaymentMethodEnum paymentMethod;

  @CreationTimestamp
  @Column(name = "TransactionTime", nullable = false, updatable = false)
  LocalDateTime transactionTime;

  @Column(name = "TransactionCode", length = 150)
  String transactionCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", nullable = false, length = 50)
  PaymentStatusEnum status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ProcessedByStaffId", nullable = false)
  @JsonBackReference
  User processedByStaff;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RenterId", nullable = false)
  private User user;
}
