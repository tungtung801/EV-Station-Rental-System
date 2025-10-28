package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    @NotBlank(message = "RentalId is required")
    Integer rentalId;

    @NotBlank(message = "UserId is required")
    Integer userId;

    @NotBlank(message = "paymentMethod is required")
    String paymentMethod;

}
