package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.UserProfileStatusEnum;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.JwtService;
import spring_boot.project_swp.service.RoleService;
import spring_boot.project_swp.service.UserService;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

  final UserRepository userRepository;
  final RoleService roleService;
  final UserMapper userMapper;
  final UserProfileRepository userProfileRepository;
  final JwtService jwtService;
  final StationRepository stationRepository;
  final FileStorageService fileStorageService;

  @Override
  public UserRegistrationResponse register(UserRegistrationRequest request) {
    return registerUser(request, "User");
  }

  @Override
  public UserRegistrationResponse registerStaff(UserRegistrationRequest request) {
    return registerUser(request, "staff");
  }

  private UserRegistrationResponse registerUser(UserRegistrationRequest request, String roleName) {
    if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
      throw new ConflictException("Email or phone number already in use");
    }
    User user = userMapper.toUser(request);
    user.setRole(roleService.getRoleByName(roleName));

    if ("staff".equalsIgnoreCase(roleName) && request.getStationId() != null) {
      Station station =
          stationRepository
              .findById(request.getStationId())
              .orElseThrow(() -> new NotFoundException("Station not found"));
      user.setStation(station);
    }

    User saved = userRepository.save(user);

    UserProfile userProfile = new UserProfile();
    userProfile.setUser(saved);
    userProfileRepository.save(userProfile);

    return new UserRegistrationResponse(saved.getUserId(), saved.getEmail());
  }

  @Override
  public UserLoginResponse login(UserLoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ConflictException("Invalid email or password"));
    if (!user.getPassword().equals(request.getPassword())) {
      throw new ConflictException("Invalid email or password");
    }
    String jwtToken = jwtService.generateToken(user);
    UserLoginResponse userLoginResponse = userMapper.toUserLoginResponse(user);
    userLoginResponse.setAccessToken(jwtToken);
    return userLoginResponse;
  }

  @Override
  public List<UserResponse> getAllUsers() {
    List<User> users = userRepository.findAllByRole_RoleNameIgnoreCase("user");
    List<UserResponse> userResponses = new ArrayList<>();
    for (User user : users) {
      userResponses.add(userMapper.toUserResponse(user));
    }
    return userResponses;
  }

  @Override
  public List<UserResponse> getAllStaff() {
    List<User> staffs = userRepository.findAllByRole_RoleNameIgnoreCase("staff");
    List<UserResponse> staffResponses = new ArrayList<>();
    for (User staff : staffs) {
      staffResponses.add(userMapper.toUserResponse(staff));
    }
    return staffResponses;
  }

  @Override
  public UserResponse getUserById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    return userMapper.toUserResponse(user);
  }

  @Override
  public UserResponse getUserByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    return userMapper.toUserResponse(user);
  }

  @Override
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User not found with id: " + userId);
    }
    userRepository.deleteById(userId);
  }

  @Override
  public UserResponse updateUser(Long userId, UserUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

    if (request.getEmail() != null
        && !request.getEmail().equals(user.getEmail())
        && userRepository.existsByEmailOrPhoneNumber(request.getEmail(), null)) {
      throw new ConflictException("Email already in use");
    }

    if (request.getPhoneNumber() != null
        && !request.getPhoneNumber().equals(user.getPhoneNumber())
        && userRepository.existsByEmailOrPhoneNumber(null, request.getPhoneNumber())) {
      throw new ConflictException("Phone number already in use");
    }

    if (request.getEmail() != null) user.setEmail(request.getEmail());
    if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
    if (request.getFullName() != null) user.setFullName(request.getFullName());
    if (request.getPassword() != null) user.setPassword(request.getPassword());

    User saved = userRepository.save(user);
    return userMapper.toUserResponse(saved);
  }

  @Override
  public void updatePassword(String email, String newPassword) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    user.setPassword(newPassword);
    userRepository.save(user);
  }

  @Override
  public void uploadDocumentImage(Long userId, String documentType, MultipartFile file) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

    UserProfile userProfile =
        userProfileRepository
            .findByUser(user)
            .orElseThrow(
                () -> new NotFoundException("User profile not found for user id: " + userId));

    String fileUrl = fileStorageService.saveFile(file);

    if ("drivingLicense".equalsIgnoreCase(documentType)) {
      userProfile.setDrivingLicenseUrl(fileUrl);
    } else if ("idCard".equalsIgnoreCase(documentType)) {
      userProfile.setIdCardUrl(fileUrl);
    }

    userProfile.setStatus(UserProfileStatusEnum.PENDING);
    userProfileRepository.save(userProfile);
  }
}
