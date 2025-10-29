package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.UserProfileStatusEnum;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
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

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
            throw new ConflictException("Email or phone number already in use");
        }
        User user = userMapper.toUser(request);
        user.setRole(roleService.getRoleByName("User"));
        User saved = userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(saved);
        userProfileRepository.save(userProfile);

        return new UserRegistrationResponse(saved.getUserId(), saved.getEmail());
    }


    @Override
    public UserRegistrationResponse registerStaff(UserRegistrationRequest request) {
        if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
            throw new ConflictException("Email or phone number already in use");
        }
        User user = userMapper.toUser(request);
        user.setRole(roleService.getRoleByName("staff"));
        User saved = userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(saved);
        userProfileRepository.save(userProfile);

        return new UserRegistrationResponse(saved.getUserId(), saved.getEmail());
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ConflictException("Invalid email or password"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ConflictException("Invalid email or password");
        }
        return userMapper.toUserLoginResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            if(user.getRole().getRoleName().equalsIgnoreCase("user")) {
                users.add(userMapper.toUserResponse(user));
            }
        }
        return users;
    }

    @Override
    public List<UserResponse> getAllStaff() {
        List<UserResponse> staffs = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            if(user.getRole().getRoleName().equalsIgnoreCase("staff")) {
                staffs.add(userMapper.toUserResponse(user));
            }
        }
        return staffs;
    }

    @Override
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmailOrPhoneNumber(request.getEmail(), null)) {
            throw new ConflictException("Email already in use");
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByEmailOrPhoneNumber(null, request.getPhoneNumber())) {
            throw new ConflictException("Phone number already in use");
        }

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getFullName() != null) user.setFullName(request.getFullName());

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public void uploadDocumentImage(Integer userId, String documentType, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("User profile not found for user id: " + userId));

        // Logic to save the file and get its URL (this part is simplified for now)
        // In a real application, you would save the file to a storage service (e.g., AWS S3, local disk)
        String fileUrl = "uploads/" + file.getOriginalFilename(); // Placeholder

        if ("drivingLicense".equalsIgnoreCase(documentType)) {
            userProfile.setDrivingLicenseUrl(fileUrl);
        } else if ("idCard".equalsIgnoreCase(documentType)) {
            userProfile.setIdCardUrl(fileUrl);
        }

        userProfile.setStatus(UserProfileStatusEnum.PENDING.name());
        userProfileRepository.save(userProfile);
    }

    public User FindUserByUserId(@NotBlank(message = "UserId is required") Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }
}



