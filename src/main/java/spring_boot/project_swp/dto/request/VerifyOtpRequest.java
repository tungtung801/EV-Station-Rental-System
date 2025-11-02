package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyOtpRequest {
  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Invalid email format")
  String email;

  @NotBlank(message = "OTP cannot be blank")
  String otp;
}
