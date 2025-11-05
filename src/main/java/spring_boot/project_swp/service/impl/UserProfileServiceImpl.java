package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.DocumentUploadRequest;
import spring_boot.project_swp.dto.request.UserProfileRejectionRequest;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileServiceImpl implements UserProfileService {

  final UserProfileRepository userProfileRepository;
  final UserProfileMapper userProfileMapper;
  final UserRepository userRepository;
  final FileStorageService fileStorageService;

  @Override
  public UserProfileResponse getUserProfileById(Long profileId) {
    UserProfile userProfile =
        userProfileRepository
            .findById(profileId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found with ID: " + profileId));
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  public UserProfileResponse getUserProfileByUserId(Long userId) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));
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
    List<UserProfile> userProfiles =
        userProfileRepository.findAllByStatus(UserProfileStatusEnum.PENDING);
    List<UserProfileResponse> pendingUserProfiles = new ArrayList<>();
    for (UserProfile userProfile : userProfiles) {
      pendingUserProfiles.add(userProfileMapper.toUserProfileResponse(userProfile));
    }
    return pendingUserProfiles;
  }

  @Override
  public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseGet(
                () -> {
                  User user =
                      userRepository
                          .findById(userId)
                          .orElseThrow(
                              () -> new NotFoundException("User not found with ID: " + userId));
                  UserProfile newProfile = new UserProfile();
                  newProfile.setUser(user);
                  return newProfile;
                });

    // Handle driving license file upload
    if (request.getDrivingLicenseFile() != null && !request.getDrivingLicenseFile().isEmpty()) {
      String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
      userProfile.setDrivingLicenseUrl(drivingLicenseUrl);
      userProfile.setStatus(UserProfileStatusEnum.PENDING); // Set status to PENDING
    }

    // Handle ID card file upload
    if (request.getIdCardFile() != null && !request.getIdCardFile().isEmpty()) {
      String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
      userProfile.setIdCardUrl(idCardUrl);
      userProfile.setStatus(UserProfileStatusEnum.PENDING); // Set status to PENDING
    }

    userProfileMapper.updateUserProfileFromRequest(request, userProfile);

    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  public void deleteUserProfile(Long profileId) {
    if (!userProfileRepository.existsById(profileId)) {
      throw new NotFoundException("User profile not found with ID: " + profileId);
    }
    userProfileRepository.deleteById(profileId);
  }

  @Override
  public void uploadVerificationDocuments(Long userId, DocumentUploadRequest request) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));

    if (request.getIdCardFile() != null && !request.getIdCardFile().isEmpty()) {
      String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
      userProfile.setIdCardUrl(idCardUrl);
    }

    if (request.getDrivingLicenseFile() != null && !request.getDrivingLicenseFile().isEmpty()) {
      String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
      userProfile.setDrivingLicenseUrl(drivingLicenseUrl);
    }

    userProfile.setStatus(UserProfileStatusEnum.PENDING);
    userProfileRepository.save(userProfile);
  }

  @Override
  public UserProfileResponse getUserProfileStatus(Long userId) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  public UserProfileResponse approveUserProfile(Long userId) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));

    userProfile.setStatus(UserProfileStatusEnum.VERIFIED);
    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  public UserProfileResponse rejectUserProfile(Long userId, UserProfileRejectionRequest request) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));

    userProfile.setStatus(UserProfileStatusEnum.REJECTED);
    userProfile.setReason(request.getReason());
    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  public UserProfileVerificationResponse verifyOrRejectUserProfile(
      UserProfileVerificationRequest request) {
    UserProfile userProfile =
        userProfileRepository
            .findById(request.getProfileId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User profile not found with ID: " + request.getProfileId()));

    if (request.getIsVerified()) {
      userProfile.setStatus(UserProfileStatusEnum.VERIFIED);
    } else {
      userProfile.setStatus(UserProfileStatusEnum.REJECTED);
      userProfile.setReason(request.getReason());
    }
    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileVerificationResponse(userProfile);
  }
}
