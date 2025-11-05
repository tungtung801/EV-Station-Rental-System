package spring_boot.project_swp.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.BookingStatusEnum;

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
  LocalDateTime startTime;
  LocalDateTime endTime;
  java.math.BigDecimal depositPercent;
  java.math.BigDecimal expectedTotal;
  BookingStatusEnum status;
  LocalDateTime createdAt;
}
