package spring_boot.project_swp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.DiscountTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountResponse {

    Long discountId;
    String code;
    String description;

    // --- SỬA ĐOẠN NÀY ---
    DiscountTypeEnum discountType;
    BigDecimal value;
    // --------------------

    LocalDateTime startDate;
    LocalDateTime endDate;
    Integer minRentalDuration;
    BigDecimal maxDiscountAmount;
    Integer usageLimit;
    Integer currentUsage;
    Boolean isActive;
}