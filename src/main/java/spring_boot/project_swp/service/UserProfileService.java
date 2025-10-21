package spring_boot.project_swp.service;

import java.util.List;

import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;

public interface UserProfileService {
    UserProfileResponse createUserProfile(UserProfileRequest request);
    UserProfileResponse getUserProfileById(Integer profileId);
    UserProfileResponse getUserProfileByUserId(Integer userId);
    List<UserProfileResponse> getAllUserProfiles();

    List<UserProfileResponse> getAllPendingUserProfiles();
    UserProfileResponse updateUserProfile(Integer profileId, UserProfileRequest request);
    void deleteUserProfile(Integer profileId);
    UserProfileVerificationResponse verifyOrRejectUserProfile(UserProfileVerificationRequest request);
}