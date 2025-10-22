package spring_boot.project_swp.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalResponse {
    Integer rentalId;
    Integer bookingId;
    Integer renterId;
    String renterName;
    Integer vehicleId;
    String vehicleModel;
    Integer pickupStationId;
    String pickupStationName;
    Integer returnStationId;
    String returnStationName;
    Integer pickupStaffId;
    String pickupStaffName;
    Integer returnStaffId;
    String returnStaffName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Double totalCost;
    String status;
    String contractUrl;
    LocalDateTime createdAt;
}