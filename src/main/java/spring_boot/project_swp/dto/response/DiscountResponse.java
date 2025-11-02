package spring_boot.project_swp.dto.response;

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
public class DiscountResponse {

  Long discountId;
  String code;
  String description;
  BigDecimal amountPercentage;
  BigDecimal amountFixed;
  LocalDateTime startDate;
  LocalDateTime endDate;
  Integer minRentalDuration;
  BigDecimal maxDiscountAmount;
  Integer usageLimit;
  Integer currentUsage;
  Boolean isActive;
}
