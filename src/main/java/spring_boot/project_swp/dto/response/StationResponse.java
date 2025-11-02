package spring_boot.project_swp.dto.response;

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
public class StationResponse {
  Long stationId;
  String stationName;
  String address;
  BigDecimal latitude;
  BigDecimal longitude;
  int totalDocks;
  int availableDocks;
  boolean isActive;
  String city;
  String district;
  String ward;
}
