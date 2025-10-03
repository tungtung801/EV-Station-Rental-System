package spring_boot.project_swp.dto.request;

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
public class LocationUpdateRequest {
    String locationName;
    String locationType;
    String address;

    BigDecimal latitude;
    BigDecimal longitude;
    BigDecimal radius;

    Integer parentLocationId; // để update location cha cho location hiện tại
}
