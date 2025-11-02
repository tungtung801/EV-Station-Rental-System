package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class DiscountRequest {

  @NotBlank(message = "Code cannot be blank")
  @Size(max = 50, message = "Code must not exceed 50 characters")
  String code;

  String description;

  @DecimalMin(
      value = "0.0",
      inclusive = true,
      message = "Percentage amount must be greater than or equal to 0")
  BigDecimal amountPercentage;

  @DecimalMin(
      value = "0.0",
      inclusive = true,
      message = "Fixed amount must be greater than or equal to 0")
  BigDecimal amountFixed;

  @NotNull(message = "Start date cannot be null")
  @FutureOrPresent(message = "Start date must be in the present or future")
  LocalDateTime startDate;

  @NotNull(message = "End date cannot be null")
  LocalDateTime endDate;

  Integer minRentalDuration;

  @DecimalMin(
      value = "0.0",
      inclusive = true,
      message = "Maximum discount amount must be greater than or equal to 0")
  BigDecimal maxDiscountAmount;

  Integer usageLimit;

  Boolean isActive;
}
