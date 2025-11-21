package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.entity.UserProfile;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

  // Create Profile
  @Mapping(target = "profileId", ignore = true)
  @Mapping(target = "drivingLicenseUrl", ignore = true)
  @Mapping(target = "idCardUrl", ignore = true)
  UserProfile toUserProfile(UserProfileRequest request);

  // Get Profile (Flattening Data)
  @Mapping(source = "user.userId", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.phoneNumber", target = "phoneNumber")
  UserProfileResponse toUserProfileResponse(UserProfile userProfile);

  // --- ĐÃ XÓA HÀM toUserProfileVerificationResponse (VÌ DTO ĐÃ XÓA) ---

  // Update Profile
  @Mapping(target = "profileId", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "drivingLicenseUrl", ignore = true)
  @Mapping(target = "idCardUrl", ignore = true)
  @Mapping(target = "status", ignore = true)
  void updateUserProfileFromRequest(
      UserProfileRequest request, @MappingTarget UserProfile userProfile);
}
