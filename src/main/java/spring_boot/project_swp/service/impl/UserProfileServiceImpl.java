package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.UserProfileRejectionRequest;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
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
      // Filter: Exclude Admin and Staff - only show regular Users
      if (userProfile.getUser() != null &&
          userProfile.getUser().getRole() != null) {
        String roleName = userProfile.getUser().getRole().getRoleName();
        // Exclude "Admin" and "Staff" roles - only show "User" roles
        if (roleName != null && !roleName.equalsIgnoreCase("Admin") && !roleName.equalsIgnoreCase("Staff")) {
          pendingUserProfiles.add(userProfileMapper.toUserProfileResponse(userProfile));
        }
      }
    }
    return pendingUserProfiles;
  }

  @Override
  @Transactional
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
                  newProfile.setStatus(UserProfileStatusEnum.UNVERIFIED);
                  return newProfile;
                });

    // Logic upload ảnh nằm ở đây rồi -> Không cần hàm riêng nữa
    if (request.getDrivingLicenseFile() != null && !request.getDrivingLicenseFile().isEmpty()) {
      String drivingLicenseUrl = fileStorageService.saveFile(request.getDrivingLicenseFile());
      userProfile.setDrivingLicenseUrl(drivingLicenseUrl);

      // Admin and Staff should always have VERIFIED status, not PENDING
      User user = userProfile.getUser();
      if (user != null && user.getRole() != null) {
        String roleName = user.getRole().getRoleName();
        if (roleName != null && (roleName.equalsIgnoreCase("Admin") || roleName.equalsIgnoreCase("Staff"))) {
          userProfile.setStatus(UserProfileStatusEnum.VERIFIED);
        } else {
          userProfile.setStatus(UserProfileStatusEnum.PENDING);
        }
      } else {
        userProfile.setStatus(UserProfileStatusEnum.PENDING);
      }
    }

    if (request.getIdCardFile() != null && !request.getIdCardFile().isEmpty()) {
      String idCardUrl = fileStorageService.saveFile(request.getIdCardFile());
      userProfile.setIdCardUrl(idCardUrl);

      // Admin and Staff should always have VERIFIED status, not PENDING
      User user = userProfile.getUser();
      if (user != null && user.getRole() != null) {
        String roleName = user.getRole().getRoleName();
        if (roleName != null && (roleName.equalsIgnoreCase("Admin") || roleName.equalsIgnoreCase("Staff"))) {
          userProfile.setStatus(UserProfileStatusEnum.VERIFIED);
        } else {
          userProfile.setStatus(UserProfileStatusEnum.PENDING);
        }
      } else {
        userProfile.setStatus(UserProfileStatusEnum.PENDING);
      }
    }

    userProfileMapper.updateUserProfileFromRequest(request, userProfile);

    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  @Transactional
  public void deleteUserProfile(Long profileId) {
    if (!userProfileRepository.existsById(profileId)) {
      throw new NotFoundException("User profile not found with ID: " + profileId);
    }
    userProfileRepository.deleteById(profileId);
  }

  // ĐÃ XÓA uploadVerificationDocuments

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
  @Transactional
  public UserProfileResponse approveUserProfile(Long userId) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user ID: " + userId));

    userProfile.setStatus(UserProfileStatusEnum.VERIFIED);
    userProfile.setReason(null);
    userProfileRepository.save(userProfile);
    return userProfileMapper.toUserProfileResponse(userProfile);
  }

  @Override
  @Transactional
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
  public Long getUserIdByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    return user.getUserId();
  }
}
