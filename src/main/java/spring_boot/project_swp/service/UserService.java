package spring_boot.project_swp.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
// Import đúng cái DTO em vừa tạo
import spring_boot.project_swp.dto.request.StaffRegistrationRequest;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;

public interface UserService {
    // Đăng ký cho Khách (Dùng UserRegistrationRequest)
    UserRegistrationResponse registerCustomer(UserRegistrationRequest request);

    // Tạo Nhân viên (Dùng StaffRegistrationRequest) <--- SỬA DÒNG NÀY
    UserRegistrationResponse createStaff(StaffRegistrationRequest request);

    UserLoginResponse login(UserLoginRequest request);

    // ... Các hàm CRUD khác giữ nguyên ...
    List<UserResponse> getAllUsers();
    List<UserResponse> getAllStaff();
    UserResponse getUserById(Long userId);
    UserResponse getUserByEmail(String email);
    void deleteUser(Long userId);
    UserResponse updateUser(Long userId, UserUpdateRequest request);
    void updatePassword(String email, String newPassword);
    void uploadDocumentImage(Long userId, String documentType, MultipartFile file);
}