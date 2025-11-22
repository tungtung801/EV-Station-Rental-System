package spring_boot.project_swp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
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

  // User info
  Long userId;
  String userName;
  String userPhone; // Thêm cái này cho Staff tiện gọi điện

  // Vehicle info
  VehicleResponse vehicle;

  BookingTypeEnum bookingType;
  LocalDateTime startTime;
  LocalDateTime endTime;

  BigDecimal totalAmount; // <--- CÁI NÀY MỚI LÀ CHUẨN

  BookingStatusEnum status;
  LocalDateTime createdAt;

  // Rental info (Nếu có)
  RentalResponse rental;

  String paymentUrl; // URL thanh toán (nếu có)
}
