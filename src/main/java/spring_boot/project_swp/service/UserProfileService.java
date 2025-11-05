package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;

public interface UserProfileService {
  // UserProfileResponse createUserProfile(UserProfileRequest request);
  UserProfileResponse getUserProfileById(Long profileId);

  UserProfileResponse getUserProfileByUserId(Long userId);

  List<UserProfileResponse> getAllUserProfiles();

  List<UserProfileResponse> getAllPendingUserProfiles();

  UserProfileResponse updateUserProfile(Long profileId, UserProfileRequest request);

  void deleteUserProfile(Long profileId);

  UserProfileVerificationResponse verifyOrRejectUserProfile(UserProfileVerificationRequest request);

  void uploadVerificationDocuments(
      Long userId, spring_boot.project_swp.dto.request.DocumentUploadRequest request);

  UserProfileResponse getUserProfileStatus(Long userId);

  UserProfileResponse approveUserProfile(Long userId);

  UserProfileResponse rejectUserProfile(
      Long userId, spring_boot.project_swp.dto.request.UserProfileRejectionRequest request);
}
