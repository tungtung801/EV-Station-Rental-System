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
    @Pattern(regexp = "^[\\p{L}\\p{M}\\s]+$", message = "Full name can only contain letters and spaces")
    String fullName;

    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must be at most 100 characters long")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(0(3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-46-9])\\d{7}|(02[0-9])\\d{8})$", message = "Invalid Vietnamese phone number")
    String phoneNumber;

    // Password is not updated directly via this request for security reasons, 
    // a separate change password mechanism should be used.
    // String password;
}