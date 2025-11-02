package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  String email;

  @NotBlank(message = "Password is required")
  String password;
}
