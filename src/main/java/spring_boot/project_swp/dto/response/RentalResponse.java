package spring_boot.project_swp.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalResponse {
  Long rentalId;
  Long bookingId;
  Long renterId;
  String renterName;
  Long vehicleId;
  String vehicleModel;
  Long pickupStationId;
  String pickupStationName;
  Long returnStationId;
  String returnStationName;
  Long pickupStaffId;
  String pickupStaffName;
  Long returnStaffId;
  String returnStaffName;
  LocalDateTime startTime;
  LocalDateTime endTime;
  Double totalCost;
  String status;
  String contractUrl;
  LocalDateTime createdAt;
}
