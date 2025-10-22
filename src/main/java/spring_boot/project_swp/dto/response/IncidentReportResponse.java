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
public class IncidentReportResponse {

    Long reportId;
    Long rentalId;
    Long vehicleId;
    Long userId;
    Long checkId;
    String description;
    String status;
    LocalDateTime reportDate;
    String imageUrls;
}