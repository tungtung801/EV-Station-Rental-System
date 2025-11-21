package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.UserProfileRejectionRequest;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;

public interface UserProfileService {

  UserProfileResponse getUserProfileById(Long profileId);

  UserProfileResponse getUserProfileByUserId(Long userId);

  List<UserProfileResponse> getAllUserProfiles();

  List<UserProfileResponse> getAllPendingUserProfiles();

  // Update thông tin (Gồm cả up ảnh bằng lái/CCCD)
  UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request);

  void deleteUserProfile(Long profileId);

  UserProfileResponse getUserProfileStatus(Long userId);

  UserProfileResponse approveUserProfile(Long userId);

  UserProfileResponse rejectUserProfile(Long userId, UserProfileRejectionRequest request);

  Long getUserIdByEmail(String email);
}
