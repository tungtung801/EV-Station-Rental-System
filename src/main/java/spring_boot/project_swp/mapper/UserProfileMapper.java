package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;
import spring_boot.project_swp.entity.UserProfile;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

  @Mapping(target = "profileId", ignore = true)
  @Mapping(target = "drivingLicenseUrl", ignore = true)
  @Mapping(target = "idCardUrl", ignore = true)
  @Mapping(source = "userId", target = "user.userId")
  UserProfile toUserProfile(UserProfileRequest request);

  @Mapping(source = "user.userId", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.phoneNumber", target = "phoneNumber")
  UserProfileResponse toUserProfileResponse(UserProfile userProfile);

  @Mapping(source = "profileId", target = "profileId")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "reason", target = "reason")
  UserProfileVerificationResponse toUserProfileVerificationResponse(UserProfile userProfile);

  @Mapping(target = "profileId", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "drivingLicenseUrl", ignore = true)
  @Mapping(target = "idCardUrl", ignore = true)
  void updateUserProfileFromRequest(
      UserProfileRequest request, @MappingTarget UserProfile userProfile);
}
