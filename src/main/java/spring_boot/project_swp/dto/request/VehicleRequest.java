package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRequest {
    @NotNull(message = "Model ID cannot be null")
    private int modelId;
    @NotNull(message = "Station ID cannot be null")
    private int stationId;
    @NotBlank(message = "License Plate cannot be null")
    @Size(max = 20)
    private String licensePlate;
    @NotNull(message = "Battery Capacity cannot be null")
    @Min(value = 0, message = "Battery Capacity must be non-negative")
    private String batteryCapacity;
    @NotNull(message = "Current Battery cannot be null")
    @Min(value = 0, message = "Current Battery must be non-negative")
    @Max(value = 100, message = "Current Battery cannot exceed 100")
    private int currentBattery;
    @NotBlank(message = "Status cannot be null")
    private String vehicleStatus;
    @NotNull(message = "Price Per Hour cannot be null")
    @Positive(message = "Price Per Hour must be positive")
    private double pricePerHour;
}
