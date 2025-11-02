package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationAddingRequest {
  @NotBlank(message = "Station name cannot be blank")
  String stationName;

  @NotBlank(message = "Address cannot be blank")
  String address;

  @NotNull(message = "Latitude cannot be null")
  BigDecimal latitude;

  @NotNull(message = "Longitude cannot be null")
  BigDecimal longitude;

  @Min(value = 1, message = "Total docks must be greater than 0")
  int totalDocks;

  @NotNull(message = "Location ID cannot be null")
  Long locationId;
}
