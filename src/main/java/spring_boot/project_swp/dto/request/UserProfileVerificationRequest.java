package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileVerificationRequest {
    @NotBlank(message = "User ID is required")
    String userId;

    @NotBlank(message = "Status is required (VERIFIED/REJECTED)")
    String status;

    String reason;
}