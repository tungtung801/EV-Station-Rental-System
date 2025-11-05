package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {

  Long bookingId;
  Long rentalId;

  @NotNull(message = "Amount cannot be null")
  @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
  BigDecimal amount;

  @NotNull(message = "Payment type cannot be null")
  PaymentTypeEnum paymentType;

  @NotNull(message = "Payment method cannot be null")
  PaymentMethodEnum paymentMethod;

  Long confirmedById;

  String note;

  String transactionCode;

  Long payerId;

  @NotNull(message = "User ID cannot be null")
  Long userId;
}
