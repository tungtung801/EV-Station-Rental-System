package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationAddingRequest {
  @NotBlank(message = "Location name is required")
  @Pattern(regexp = "^[\\p{L}0-9\\s,.\\-()/&]{2,100}$")
  String locationName;

  @NotBlank(message = "Location type is required")
  @Pattern(regexp = "^[\\p{L}\\s]{2,50}$")
  String locationType;
}
