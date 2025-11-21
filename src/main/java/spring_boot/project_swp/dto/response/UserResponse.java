package spring_boot.project_swp.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.UserProfileStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
  Long userId;
  String fullName;
  String email;
  String phoneNumber;
  String roleName;
  Boolean accountStatus;
  LocalDateTime createdAt;
  String stationName;
  UserProfileStatusEnum kycStatus;
}
