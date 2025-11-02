package spring_boot.project_swp.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
  Long paymentId;
  Long rentalId;
  PaymentTypeEnum paymentType;
  double amount;
  PaymentMethodEnum paymentMethod;
  String transactionTime;
  String transactionCode;
  PaymentStatusEnum status;
}
