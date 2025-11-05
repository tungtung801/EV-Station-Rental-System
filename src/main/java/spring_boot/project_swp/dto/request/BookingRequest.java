package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
  @NotNull(message = "User ID cannot be null")
  Long userId;

  @NotNull(message = "Vehicle ID cannot be null")
  Long vehicleId;

  @NotNull(message = "Start time cannot be null")
  @FutureOrPresent(message = "Start time must be in the present or future")
  LocalDateTime startTime;

  @NotNull(message = "End time cannot be null")
  @FutureOrPresent(message = "End time must be in the present or future")
  LocalDateTime endTime;
}
