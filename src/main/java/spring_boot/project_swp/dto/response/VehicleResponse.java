package spring_boot.project_swp.dto.response;

import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.VehicleStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    Long vehicleId;
    String licensePlate;
    String color; // <--- Đừng quên tôi
    int currentBattery;
    VehicleStatusEnum vehicleStatus;
    BigDecimal pricePerHour;
    String imageUrl;

    // Thông tin Model (Hãng, Loại, Dung lượng pin chuẩn)
    ModelInfo model;

    // Thông tin Station (Vị trí hiện tại)
    StationInfo currentStation;

    @Data
    @AllArgsConstructor // Thêm cái này cho tiện mapper
    @NoArgsConstructor
    public static class ModelInfo {
        Long modelId;
        String modelName;
        String brand;
        String type;
        int capacityKWh;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StationInfo {
        Long stationId;
        String stationName;
        String address;
    }
}