package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {
  @NotNull(message = "Rental ID cannot be null")
  Long rentalId;

  @NotNull(message = "Amount cannot be null")
  Double amount;

  @NotBlank(message = "Payment method cannot be blank")
  String paymentMethod;

  String transactionId;
  Long processedByStaffId;
}
