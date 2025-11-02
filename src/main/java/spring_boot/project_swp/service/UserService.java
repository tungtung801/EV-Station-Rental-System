package spring_boot.project_swp.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;

public interface UserService {
  // Authentication
  UserRegistrationResponse register(UserRegistrationRequest request);

  UserRegistrationResponse registerStaff(UserRegistrationRequest request);

  UserLoginResponse login(UserLoginRequest request);

  // CRUD
  List<UserResponse> getAllUsers();

  List<UserResponse> getAllStaff();

  UserResponse getUserById(Long userId);

  UserResponse getUserByEmail(String email);

  void deleteUser(Long userId);

  UserResponse updateUser(Long userID, UserUpdateRequest request);

  void updatePassword(String email, String newPassword);

  void uploadDocumentImage(Long userId, String documentType, MultipartFile file);
}
