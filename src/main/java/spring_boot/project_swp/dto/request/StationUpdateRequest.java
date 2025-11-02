package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class StationUpdateRequest {
  @NotBlank(message = "StationName cannot be blank")
  String stationName;

  @NotBlank(message = "Station address cannot be blank")
  String address;

  @NotNull(message = "Latitude cannot be null")
  BigDecimal latitude;

  @NotNull(message = "Longitude cannot be null")
  BigDecimal longitude;

  @Min(value = 1, message = "TotalDocs must greater than 0")
  int totalDocks;

  @NotNull(message = "AvailableDocks cannot be null")
  int availableDocks;

  @NotNull(message = "District / Ward Id cannot be null")
  Long locationId;
}
