package spring_boot.project_swp.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "OTP_NOT_BLANK")
    String otp;

    @NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
    @Size(min = 8, message = "PASSWORD_MIN_SIZE")
    String newPassword;
}