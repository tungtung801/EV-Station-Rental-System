package spring_boot.project_swp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleModelResponse {
  Long modelId;
  String modelName;
  String brand;
  String type;
  int capacityKWh;
  String description;
}
