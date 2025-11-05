package spring_boot.project_swp.dto.request;

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
  @Size(max = 100, message = "Model name cannot exceed 100 characters")
  private String modelName;

  @Size(max = 100, message = "Brand cannot exceed 100 characters")
  private String brand;

  @Size(max = 100, message = "Type cannot exceed 100 characters")
  private String type;

  private Integer capacityKWh;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;
}
