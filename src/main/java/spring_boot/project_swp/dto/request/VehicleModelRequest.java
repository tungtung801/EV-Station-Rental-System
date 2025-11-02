package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleModelRequest {
  @NotBlank(message = "Model name cannot be blank")
  @Size(max = 100, message = "Model name cannot exceed 100 characters")
  private String modelName;

  @NotBlank(message = "Brand cannot be blank")
  @Size(max = 100, message = "Brand cannot exceed 100 characters")
  private String brand;

  @NotBlank(message = "Type cannot be blank")
  @Size(max = 100, message = "Type cannot exceed 100 characters")
  private String type;

  @NotNull(message = "Capacity KWh cannot be null")
  private int capacityKWh;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;
}
