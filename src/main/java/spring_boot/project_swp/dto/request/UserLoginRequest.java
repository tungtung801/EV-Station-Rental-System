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
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters long")
    String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password;
}
