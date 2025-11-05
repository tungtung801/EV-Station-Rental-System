package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileRejectionRequest {
  @NotBlank(message = "Reason cannot be blank")
  String reason;
}
