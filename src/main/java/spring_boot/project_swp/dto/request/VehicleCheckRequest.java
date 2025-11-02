package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VehicleCheckRequest {

  @NotNull(message = "Rental ID cannot be null")
  Long rentalId;

  @NotNull(message = "Staff ID cannot be null")
  Long staffId;

  @NotBlank(message = "Check type cannot be blank")
  String checkType;

  String notes;

  String imageUrls;
}
