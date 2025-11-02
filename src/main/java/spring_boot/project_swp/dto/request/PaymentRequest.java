package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotNull;
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

  @NotNull(message = "RentalId is required")
  Long rentalId;

  @NotNull(message = "UserId is required")
  Long userId;

  @NotNull(message = "PaymentType is required")
  PaymentTypeEnum paymentType;

  @NotNull(message = "PaymentMethod is required")
  PaymentMethodEnum paymentMethod;

  Long staffId;
}
