package spring_boot.project_swp.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.BookingTypeEnum;
import spring_boot.project_swp.dto.response.VehicleResponse;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    Integer bookingId;
    Integer userId;
    String userName;
    VehicleResponse vehicle;
    BookingTypeEnum bookingType;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Double totalAmount;
    BookingStatusEnum status;
    LocalDateTime createdAt;
}