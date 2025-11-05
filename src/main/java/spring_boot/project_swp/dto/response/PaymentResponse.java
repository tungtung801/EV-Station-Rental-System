package spring_boot.project_swp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

  Long paymentId;
  Long bookingId;
  Long rentalId;
  BigDecimal amount;
  PaymentTypeEnum paymentType;
  PaymentMethodEnum paymentMethod;
  PaymentStatusEnum status;
  Long confirmedById;
  String note;
  LocalDateTime createdAt;
  LocalDateTime confirmedAt;
  String transactionCode;
  Long payerId;
}
