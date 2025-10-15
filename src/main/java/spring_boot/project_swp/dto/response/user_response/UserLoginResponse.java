package spring_boot.project_swp.dto.response.user_response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginResponse {
    Integer userId;
    String fullName;
    String email;
    String phoneNumber;
    String roleName;
}
