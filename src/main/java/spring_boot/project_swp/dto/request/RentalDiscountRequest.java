package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class RentalDiscountRequest {

  @NotNull(message = "Rental ID cannot be null")
  Long rentalId;

  @NotNull(message = "Discount ID cannot be null")
  Long discountId;

  @DecimalMin(
      value = "0.0",
      inclusive = true,
      message = "Applied discount amount must be greater than or equal to 0")
  BigDecimal appliedAmount;

  LocalDateTime appliedAt;
}
