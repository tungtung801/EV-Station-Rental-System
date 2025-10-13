package spring_boot.project_swp.dto.response;

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
public class StationResponse {
    Integer stationId;
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
