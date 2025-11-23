package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.StaffRegistrationRequest; // <--- DTO CHUẨN
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.JwtService;
import spring_boot.project_swp.service.RoleService;
import spring_boot.project_swp.service.UserService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

  final UserRepository userRepository;
  final RoleService roleService;
  final UserMapper userMapper;
  final UserProfileRepository userProfileRepository;
  final StationRepository stationRepository;
  final JwtService jwtService;
  final FileStorageService fileStorageService;
  final PasswordEncoder passwordEncoder;

  private static final String ROLE_USER = "User";
  private static final String ROLE_STAFF = "Staff";

  // =================================================================
  // 1. ĐĂNG KÝ KHÁCH HÀNG (PUBLIC)
  // =================================================================
  @Override
  @Transactional
  public UserRegistrationResponse registerCustomer(UserRegistrationRequest request) {
    if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
      throw new ConflictException("Email or phone number already in use");
    }

    // Map từ DTO UserRegistrationRequest sang Entity
    User user = userMapper.toUser(request);

    // Mã hóa mật khẩu
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    // Set Role User
    user.setRole(roleService.getRoleEntityByName(ROLE_USER));
    user.setAccountStatus(true);

    // Lưu User
    User savedUser = userRepository.save(user);

    // Tạo Profile rỗng
    createEmptyProfile(savedUser);

    return new UserRegistrationResponse(savedUser.getUserId(), savedUser.getEmail());
  }

  // =================================================================
  // 2. TẠO NHÂN VIÊN (ADMIN) - Dùng StaffRegistrationRequest
  // =================================================================
  @Override
  @Transactional
  public UserRegistrationResponse createStaff(StaffRegistrationRequest request) {
    if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
      throw new ConflictException("Email or phone number already in use");
    }

    // Map thủ công (Vì StaffRegistrationRequest khác UserRegistrationRequest)
    User user = new User();
    user.setFullName(request.getFullName());
    user.setEmail(request.getEmail());
    user.setPhoneNumber(request.getPhoneNumber());

    // Mã hóa mật khẩu
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setAccountStatus(true);

    // Set Role Staff
    user.setRole(roleService.getRoleEntityByName(ROLE_STAFF));

    // Set Station (Lấy từ request.getStationId)
    Station station =
        stationRepository
            .findById(request.getStationId())
            .orElseThrow(
                () ->
                    new NotFoundException("Station not found with id: " + request.getStationId()));
    user.setStation(station);

    User savedUser = userRepository.save(user);
    createEmptyProfile(savedUser);

    return new UserRegistrationResponse(savedUser.getUserId(), savedUser.getEmail());
  }

  // =================================================================
  // 3. ĐĂNG NHẬP
  // =================================================================
  @Override
  public UserLoginResponse login(UserLoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ConflictException("Invalid email or password"));

    // So sánh mật khẩu đã mã hóa
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ConflictException("Invalid email or password");
    }

    if (!user.getAccountStatus()) {
      throw new ConflictException("Account is locked");
    }

    String jwtToken = jwtService.generateToken(user);
    UserLoginResponse response = userMapper.toUserLoginResponse(user);
    response.setAccessToken(jwtToken);
    return response;
  }

  // =================================================================
  // 4. QUẢN LÝ USER (CRUD)
  // =================================================================
  @Override
  public List<UserResponse> getAllUsers() {
    List<User> users = userRepository.findAllByRole_RoleNameIgnoreCase(ROLE_USER);
    List<UserResponse> userResponses = new ArrayList<>();
    for (User user : users) {
      userResponses.add(userMapper.toUserResponse(user));
    }
    return userResponses;
  }

  @Override
  public List<UserResponse> getAllStaff() {
    List<User> staffs = userRepository.findAllByRole_RoleNameIgnoreCase(ROLE_STAFF);
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
  @Transactional
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

    userMapper.updateUser(user, request);

    // Nếu có đổi mật khẩu -> Mã hóa lại
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    // Cập nhật station nếu stationId được cung cấp
    if (request.getStationId() != null) {
      Station station = stationRepository
          .findById(request.getStationId())
          .orElseThrow(() -> new NotFoundException("Station not found with id: " + request.getStationId()));
      user.setStation(station);
    }

    // Cập nhật accountStatus nếu được cung cấp (Admin ban/unban khách hàng)
    if (request.getAccountStatus() != null) {
      user.setAccountStatus(request.getAccountStatus());
    }

    User saved = userRepository.save(user);
    return userMapper.toUserResponse(saved);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User not found with id: " + userId);
    }
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional
  public void updatePassword(String email, String newPassword) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  // =================================================================
  // 5. UPLOAD KYC & HELPER
  // =================================================================
  @Override
  @Transactional
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
    } else {
      throw new IllegalArgumentException("Invalid document type");
    }

    // Reset về trạng thái chờ duyệt
    userProfile.setStatus(UserProfileStatusEnum.PENDING);
    userProfileRepository.save(userProfile);
  }

  private void createEmptyProfile(User user) {
    UserProfile profile = new UserProfile();
    profile.setUser(user);
    profile.setStatus(UserProfileStatusEnum.UNVERIFIED);
    userProfileRepository.save(profile);
  }
}
