package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class DiscountRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50, message = "Mã giảm giá không được vượt quá 50 ký tự")
    String code;

    String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Phần trăm giảm giá phải lớn hơn hoặc bằng 0")
    BigDecimal amountPercentage;

    @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền giảm giá cố định phải lớn hơn hoặc bằng 0")
    BigDecimal amountFixed;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là hiện tại hoặc tương lai")
    LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    LocalDateTime endDate;

    Integer minRentalDuration;

    @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền giảm giá tối đa phải lớn hơn hoặc bằng 0")
    BigDecimal maxDiscountAmount;

    Integer usageLimit;

    Boolean isActive;
}