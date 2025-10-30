package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.UserProfileStatusEnum;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.UserProfileMapper;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.UserProfileService;

// ... existing code ...

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileServiceImpl implements UserProfileService {

    final UserProfileRepository userProfileRepository;
    final UserProfileMapper userProfileMapper;
    final UserRepository userRepository;
    final FileStorageService fileStorageService;

    @Override
    public UserProfileResponse getUserProfileById(Integer profileId) {
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("User profile not found with ID: " + profileId));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @Override
    public UserProfileResponse getUserProfileByUserId(Integer userId) {
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new NotFoundException("User profile not found for user ID: " + userId));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @Override
    public List<UserProfileResponse> getAllUserProfiles() {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        List<UserProfileResponse> userProfileResponses = new ArrayList<>();
        for (UserProfile userProfile : userProfiles) {
            userProfileResponses.add(userProfileMapper.toUserProfileResponse(userProfile));
        }
        return userProfileResponses;
    }

    @Override
    public List<UserProfileResponse> getAllPendingUserProfiles() {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        List<UserProfileResponse> pendingUserProfiles = new ArrayList<>();
        for (UserProfile userProfile : userProfiles) {
            if (UserProfileStatusEnum.PENDING.equals(userProfile.getStatus())) {
                pendingUserProfiles.add(userProfileMapper.toUserProfileResponse(userProfile));
            }
        }
        return pendingUserProfiles;
    }

    @Override
    public UserProfileResponse updateUserProfile(Integer userId, UserProfileRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user);
                return newProfile;
            });

        // Handle driving license file upload
        if (request.getDrivingLicenseFile() != null && !request.getDrivingLicenseFile().isEmpty()) {
            String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
            userProfile.setDrivingLicenseUrl(drivingLicenseUrl);
            userProfile.setStatus(UserProfileStatusEnum.PENDING.name()); // Set status to PENDING
        }

        // Handle ID card file upload
        if (request.getIdCardFile() != null && !request.getIdCardFile().isEmpty()) {
            String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
            userProfile.setIdCardUrl(idCardUrl);
            userProfile.setStatus(UserProfileStatusEnum.PENDING.name()); // Set status to PENDING
        }

        userProfileMapper.updateUserProfileFromRequest(request, userProfile);

        userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @Override
    public void deleteUserProfile(Integer profileId) {
        if (!userProfileRepository.existsById(profileId)) {
            throw new NotFoundException("User profile not found with ID: " + profileId);
        }
        userProfileRepository.deleteById(profileId);
    }

    @Override
    public UserProfileVerificationResponse verifyOrRejectUserProfile(UserProfileVerificationRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserUserId(Integer.parseInt(request.getUserId()))
                .orElseThrow(() -> new NotFoundException("User profile not found for user ID: " + request.getUserId()));

        UserProfileStatusEnum status = UserProfileStatusEnum.valueOf(request.getStatus().toUpperCase());
        userProfile.setStatus(status.name());
        userProfile.setReason(request.getReason());
        userProfileRepository.save(userProfile);

        return UserProfileVerificationResponse.builder()
                .userId(request.getUserId())
                .status(status.name())
                .message("User profile " + status.name().toLowerCase() + " successfully.")
                .build();
    }
}