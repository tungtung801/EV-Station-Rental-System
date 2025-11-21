package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffRegistrationRequest {
  @NotBlank(message = "Full name is required")
  @Size(max = 50, message = "Full name must be at most 50 characters long")
  @Pattern(
      regexp = "^[\\p{L}\\p{M}\\s]+$",
      message = "Full name can only contain letters and spaces")
  String fullName;

  @NotBlank(message = "Email is required")
  @Size(max = 100, message = "Email must be at most 100 characters long")
  @Email(message = "Invalid email format")
  String email;

  @NotBlank(message = "Phone number is required")
  @Pattern(
      regexp = "^(0(3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-46-9])\\d{7}|(02[0-9])\\d{8})$",
      message = "Invalid Vietnamese phone number")
  String phoneNumber;

  @NotBlank(message = "Password is required")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
      message =
          "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  String password;

  @NotNull(message = "Station ID is required for Staff")
  Long stationId; // <-- ĐÂY, NÓ PHẢI NẰM Ở ĐÂY
}
