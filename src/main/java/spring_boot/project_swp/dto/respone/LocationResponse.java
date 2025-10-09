package spring_boot.project_swp.dto.respone;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {
    int locationId;
    String locationName;
    String locationType;
    BigDecimal latitude;
    BigDecimal longitude;
    BigDecimal radius;
    Integer parentLocationId;
}
