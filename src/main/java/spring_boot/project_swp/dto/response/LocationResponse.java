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
public class LocationResponse {
  Long locationId;
  String locationName;
  String locationType;
  BigDecimal latitude;
  BigDecimal longitude;
  BigDecimal radius;
  LocationResponse parent;
  boolean active;
}
