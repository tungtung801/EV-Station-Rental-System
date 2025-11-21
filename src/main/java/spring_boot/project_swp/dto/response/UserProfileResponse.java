package spring_boot.project_swp.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.UserProfileStatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
  Long profileId;
  Long userId;
  String userName;
  String email;
  String phoneNumber;
  String drivingLicenseUrl;
  String idCardUrl;
  UserProfileStatusEnum status;
  String bio;
  String reason;
}
