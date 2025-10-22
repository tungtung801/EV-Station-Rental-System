package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RentalRequest {
    @NotNull(message = "Booking ID cannot be null")
    Integer bookingId;

    @NotNull(message = "Renter ID cannot be null")
    Integer renterId;

    @NotNull(message = "Vehicle ID cannot be null")
    Integer vehicleId;

    @NotNull(message = "Pickup Station ID cannot be null")
    Integer pickupStationId;

    Integer returnStationId;

    Integer pickupStaffId;

    Integer returnStaffId;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    LocalDateTime startTime;

    @FutureOrPresent(message = "End time must be in the present or future")
    LocalDateTime endTime;

    @NotBlank(message = "Status cannot be blank")
    String status;

    String contractUrl;
}