package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Pattern;
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
public class LocationUpdateRequest {
  @Pattern(regexp = "^[\\p{L}0-9\\s,.\\-()/&]{2,100}$")
  String locationName;

  @Pattern(regexp = "^[\\p{L}\\s]{2,50}$")
  String locationType;

  BigDecimal latitude;
  BigDecimal longitude;
  BigDecimal radius;

  Long parentLocationId;

  Boolean active = true; // Default to active (true) if not specified in request
}
