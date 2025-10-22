package spring_boot.project_swp.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VehicleCheckResponse {

    Long checkId;
    Long rentalId;
    Long staffId;
    String checkType;
    LocalDateTime checkDate;
    String notes;
    String imageUrls;
}