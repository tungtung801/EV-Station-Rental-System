package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

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
import spring_boot.project_swp.service.UserProfileService;

import spring_boot.project_swp.service.FileStorageService;

import java.util.Objects;

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
    public UserProfileResponse createUserProfile(UserProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));

        if (userProfileRepository.findByUserUserId(request.getUserId()).isPresent()) {
            throw new NotFoundException("User profile already exists for user ID: " + request.getUserId());
        }

        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile.setUser(user);

        if (Objects.nonNull(request.getDrivingLicenseFile()) && !request.getDrivingLicenseFile().isEmpty()) {
            String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
            userProfile.setDrivingLicenseUrl(drivingLicenseUrl);
        }

        if (Objects.nonNull(request.getIdCardFile()) && !request.getIdCardFile().isEmpty()) {
            String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
            userProfile.setIdCardUrl(idCardUrl);
        }

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(userProfile));
    }

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
    public UserProfileResponse updateUserProfile(Integer profileId, UserProfileRequest request) {
        UserProfile existingUserProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("User profile not found with ID: " + profileId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));

        userProfileMapper.updateUserProfileFromRequest(request, existingUserProfile);
        existingUserProfile.setUser(user);

        if (Objects.nonNull(request.getDrivingLicenseFile()) && !request.getDrivingLicenseFile().isEmpty()) {
            if (Objects.nonNull(existingUserProfile.getDrivingLicenseUrl())) {
                fileStorageService.deleteFile(existingUserProfile.getDrivingLicenseUrl());
            }
            String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
            existingUserProfile.setDrivingLicenseUrl(drivingLicenseUrl);
        }

        if (Objects.nonNull(request.getIdCardFile()) && !request.getIdCardFile().isEmpty()) {
            if (Objects.nonNull(existingUserProfile.getIdCardUrl())) {
                fileStorageService.deleteFile(existingUserProfile.getIdCardUrl());
            }
            String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
            existingUserProfile.setIdCardUrl(idCardUrl);
        }

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(existingUserProfile));
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
        userProfile.setStatus(status);
        userProfile.setReason(request.getReason());
        userProfileRepository.save(userProfile);

        return UserProfileVerificationResponse.builder()
                .userId(request.getUserId())
                .status(status.name())
                .message("User profile " + status.name().toLowerCase() + " successfully.")
                .build();
    }
}