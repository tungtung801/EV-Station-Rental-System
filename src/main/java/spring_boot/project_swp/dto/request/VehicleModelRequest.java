package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleModelRequest {
  @NotBlank
  @Size(max = 100)
  String modelName;

  @NotBlank
  @Size(max = 100)
  String brand;

  @NotBlank
  @Size(max = 100)
  String type;

  @NotNull
  @Min(1)
  Integer capacityKWh; // Pin thiết kế

  @Size(max = 500)
  String description;
}
