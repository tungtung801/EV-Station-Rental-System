package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalConfirmPickupRequest {
  @NotBlank(message = "Contract URL cannot be blank")
  String contractUrl;
}
