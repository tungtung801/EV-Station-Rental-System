package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.DiscountTypeEnum; // Nhớ import

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

    // --- SỬA ĐOẠN NÀY ---
    @NotNull(message = "Discount type is required")
    DiscountTypeEnum discountType; // PERCENTAGE hoặc FIXED_AMOUNT

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be positive")
    BigDecimal value; // 10% hoặc 50.000đ
    // --------------------

    @NotNull(message = "Start date cannot be null")
    // @FutureOrPresent // Có thể bỏ nếu muốn tạo mã áp dụng ngay lập tức
    LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    LocalDateTime endDate;

    @Min(value = 0)
    Integer minRentalDuration;

    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal maxDiscountAmount;

    @Min(value = 1)
    Integer usageLimit;

    Boolean isActive; // Mặc định true nếu null
}