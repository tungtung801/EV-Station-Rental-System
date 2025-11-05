package spring_boot.project_swp.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.RentalStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalResponse {
  Long rentalId;
  Long bookingId;
  Long userId;
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
  LocalDateTime startActual;
  LocalDateTime endActual;
  java.math.BigDecimal total;
  RentalStatusEnum status;
  String contractUrl;
  LocalDateTime createdAt;
}
