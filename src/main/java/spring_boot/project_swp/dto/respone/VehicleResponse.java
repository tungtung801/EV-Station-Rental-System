package spring_boot.project_swp.dto.respone;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    private int vehicleId;
    private String licensePlate;
    private int batteryCapacity;
    private int currentBattery;
    private String vehicleStatus;
    private double pricePerHour;
    private ModelInfo model;
    private StationInfo currentStation;

    @Data
    public static class ModelInfo {
        private int modelId;
        private String modelName;
        private String brand;
    }
    @Data
    public static class StationInfo {
        private int stationId;
        private String stationName;
        private String address;
    }
}
