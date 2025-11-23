package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
  @NotBlank(message = "Full name is required")
  @Size(max = 50, message = "Full name must be at most 50 characters long")
  @Pattern(
      regexp = "^[\\p{L}\\p{M}\\s]+$",
      message = "Full name can only contain letters and spaces")
  String fullName;

  @Size(max = 100, message = "Email must be at most 100 characters long")
  @Email(message = "Invalid email format")
  String email;

  @Size(max = 15, message = "Phone number must be at most 15 characters long")
  String phoneNumber;

  @Size(min = 6, message = "Password must be at least 6 characters long")
  String password;

  // Cho phép cập nhật station cho staff
  Long stationId;

  // Cho phép admin ban/unban khách hàng (true = active, false = locked)
  Boolean accountStatus;
}
