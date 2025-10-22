package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalDiscountRequest {

    @NotNull(message = "Mã thuê xe không được để trống")
    Integer rentalId;

    @NotNull(message = "Mã giảm giá không được để trống")
    Integer discountId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền giảm giá áp dụng phải lớn hơn hoặc bằng 0")
    BigDecimal appliedAmount;

    LocalDateTime appliedAt;
}