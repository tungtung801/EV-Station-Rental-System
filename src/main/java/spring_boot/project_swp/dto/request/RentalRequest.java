package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RentalRequest {
  @NotNull(message = "Booking ID cannot be null")
  Long bookingId;

  @NotNull(message = "Renter ID cannot be null")
  Long renterId;

  @NotNull(message = "Vehicle ID cannot be null")
  Long vehicleId;

  @NotNull(message = "Pickup Station ID cannot be null")
  Long pickupStationId;

  Long returnStationId;

  Long pickupStaffId;

  Long returnStaffId;

  @NotNull(message = "Start time cannot be null")
  @FutureOrPresent(message = "Start time must be in the present or future")
  LocalDateTime startTime;

  @FutureOrPresent(message = "End time must be in the present or future")
  LocalDateTime endTime;

  @NotBlank(message = "Status cannot be blank")
  String status;

  String contractUrl;
}
