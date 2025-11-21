package spring_boot.project_swp.dto.response;

import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
  Long vehicleId;
  String licensePlate;
  int currentBattery;
  String vehicleStatus;
  BigDecimal pricePerHour;
  String imageUrl;

  // Thông tin Model (Hãng, Loại, Dung lượng pin chuẩn)
  ModelInfo model;

  // Thông tin Station (Vị trí hiện tại)
  StationInfo currentStation;

  @Data
  public static class ModelInfo {
    Long modelId;
    String modelName;
    String brand;
    String type;
    int capacityKWh; // Pin chuẩn của dòng xe
  }

  @Data
  public static class StationInfo {
    Long stationId;
    String stationName;
    String address;
  }
}
