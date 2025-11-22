package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalConfirmPickupRequest {

    @NotNull(message = "Contract image is required")
    MultipartFile contractImage;

    String pickupNote; // Ghi chú xe lúc giao

    @NotNull(message = "Current odometer is required")
    @Min(value = 0, message = "Odometer must be positive")
    Integer currentOdometer; // Odometer lúc nhận
}