package spring_boot.project_swp.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.BookingTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
  Long bookingId;
  Long userId;
  String userName;
  VehicleResponse vehicle;
  BookingTypeEnum bookingType;
  LocalDateTime startTime;
  LocalDateTime endTime;
  Double totalAmount;
  BookingStatusEnum status;
  LocalDateTime createdAt;
}
