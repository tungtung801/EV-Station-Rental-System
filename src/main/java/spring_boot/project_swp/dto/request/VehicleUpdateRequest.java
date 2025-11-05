package spring_boot.project_swp.dto.request;

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
public class VehicleUpdateRequest {
  private Long modelId;
  private Long stationId;
  private String licensePlate;
  private Integer batteryCapacity;
  private Integer currentBattery;
  private String vehicleStatus;
  private Double pricePerHour;
  private MultipartFile imageFile;
}
