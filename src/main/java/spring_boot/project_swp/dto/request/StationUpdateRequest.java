package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.StationStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationUpdateRequest {
  String stationName;

  String address;

  BigDecimal latitude;

  BigDecimal longitude;

  StationStatusEnum isActive;

  Long locationId;
}
