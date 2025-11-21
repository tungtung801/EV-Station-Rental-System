package spring_boot.project_swp.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleUpdateRequest {
  Long modelId;
  Long stationId;
  String licensePlate;
  Integer currentBattery;
  String vehicleStatus;
  Double pricePerHour;
  MultipartFile imageFile;
}
