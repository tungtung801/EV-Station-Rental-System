package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.PaymentStatusEnum;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentStatusUpdateRequest {

  @NotNull(message = "Payment status cannot be null")
  PaymentStatusEnum status;
}
