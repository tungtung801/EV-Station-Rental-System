package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.entity.VehicleStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleUpdateRequest {

    Long modelId;

    Long stationId;

    @Size(max = 20, message = "License Plate too long")
    String licensePlate;

    @Size(max = 50)
    String color;

    @Min(value = 0, message = "Battery cannot be negative")
    @Max(value = 100, message = "Battery cannot exceed 100%")
    Integer currentBattery; // Dùng Integer (Object) để có thể null

    VehicleStatusEnum vehicleStatus;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    BigDecimal pricePerHour;

    MultipartFile imageFile;
}