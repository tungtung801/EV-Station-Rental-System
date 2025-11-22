package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalReturnRequest {

    // Số Km cuối cùng (BẮT BUỘC KHÔNG NULL và KHÔNG ÂM)
    @NotNull(message = "Return Odometer is required")
    @Min(value = 0, message = "Odometer must be positive")
    Integer returnOdometer;

    // Phụ phí (Có thể là 0, nên phải dùng inclusive=true)
    @DecimalMin(value = "0.0", inclusive = true, message = "Surcharge amount must be zero or positive")
    BigDecimal surcharge;

    String returnNote;
}