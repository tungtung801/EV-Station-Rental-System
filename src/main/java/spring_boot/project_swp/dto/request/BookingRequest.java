package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.BookingTypeEnum;
import spring_boot.project_swp.entity.PaymentMethodEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
  @NotNull(message = "Vehicle ID cannot be null")
  Long vehicleId;

  @NotNull(message = "Booking type cannot be null")
  BookingTypeEnum bookingType; // ONLINE / OFFLINE

    @NotNull(message = "Payment method is required")
    PaymentMethodEnum paymentMethod; // VNPAY / CASH / WALLET

  @NotNull(message = "Start time cannot be null")
  @FutureOrPresent
  LocalDateTime startTime;

  @NotNull(message = "End time cannot be null")
  @FutureOrPresent
  LocalDateTime endTime;

  String discountCode;
}
