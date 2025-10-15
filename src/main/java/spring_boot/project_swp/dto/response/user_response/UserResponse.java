package spring_boot.project_swp.dto.response.user_response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer userId;
    String fullName;
    String email;
    String phoneNumber;
    String roleName;
    Boolean accountStatus;
    LocalDateTime createdAt;
}
