package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

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
  @Size(max = 20)
  String licensePlate;

  @NotNull(message = "Current Battery cannot be null")
  @Min(0)
  @Max(100)
  Integer currentBattery;

  @NotBlank(message = "Status cannot be null")
  String vehicleStatus;

  @NotNull @Positive double pricePerHour;


  MultipartFile imageFile;
}
