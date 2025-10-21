package spring_boot.project_swp.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRequest {
    @NotBlank(message = "EMAIL_NOT_BLANK")
    @Email(message = "INVALID_EMAIL_FORMAT")
    String email;
}