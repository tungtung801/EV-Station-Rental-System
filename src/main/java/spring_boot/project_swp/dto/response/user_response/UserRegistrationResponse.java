package spring_boot.project_swp.dto.response.user_response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationResponse {
    Integer userId;
    String email;
    String message;
}
