package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleRequest {
  @NotNull(message = "Model ID cannot be null")
  private Long modelId;

  @NotNull(message = "Station ID cannot be null")
  private Long stationId;

  @NotBlank(message = "License Plate cannot be null")
  @Size(max = 20)
  private String licensePlate;

  @NotNull(message = "Battery Capacity cannot be null")
  @Min(value = 0, message = "Battery Capacity must be non-negative")
  private Integer batteryCapacity;

  @NotNull(message = "Current Battery cannot be null")
  @Min(value = 0, message = "Current Battery must be non-negative")
  @Max(value = 100, message = "Current Battery cannot exceed 100")
  private int currentBattery;

  @NotBlank(message = "Status cannot be null")
  private String vehicleStatus;

  @NotNull(message = "Price Per Hour cannot be null")
  @Positive(message = "Price Per Hour must be positive")
  private double pricePerHour;

  private MultipartFile imageFile;
}
