package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
  @NotBlank(message = "Role name cannot be blank")
  @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
  String roleName;
}
