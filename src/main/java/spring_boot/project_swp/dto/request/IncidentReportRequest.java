package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IncidentReportRequest {

  @NotNull(message = "Rental ID cannot be null")
  Long rentalId;

  @NotNull(message = "Vehicle ID cannot be null")
  Long vehicleId;

  @NotNull(message = "User ID cannot be null")
  Long userId;

  Long checkId;

  @NotBlank(message = "Description cannot be blank")
  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  String description;

  @NotBlank(message = "Status cannot be blank")
  String status;

  String imageUrls;
}
