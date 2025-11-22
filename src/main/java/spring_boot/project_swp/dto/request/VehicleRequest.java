package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal; // Dùng cái này cho tiền!
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.entity.VehicleStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleRequest {
    @NotNull(message = "Model ID cannot be null")
    Long modelId;

    @NotNull(message = "Station ID cannot be null")
    Long stationId;

    @NotBlank(message = "License Plate cannot be null")
    @Size(max = 20, message = "License Plate too long")
    String licensePlate;


    @NotNull(message = "Current Battery cannot be null")
    @Min(value = 0, message = "Battery cannot be negative")
    @Max(value = 100, message = "Battery cannot exceed 100%")
    Integer currentBattery;

    @NotNull(message = "Status cannot be null")
    VehicleStatusEnum vehicleStatus;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal pricePerHour; // <--- Dùng BigDecimal

    MultipartFile imageFile;
}