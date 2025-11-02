package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
  @NotBlank(message = "OTP cannot be blank")
  String otp;

  @NotBlank(message = "New password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  String newPassword;
}
