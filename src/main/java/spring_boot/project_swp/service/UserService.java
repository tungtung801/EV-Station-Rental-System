package spring_boot.project_swp.service;

import java.util.List;

import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.User;


public interface UserService {
    //Authentication
    UserRegistrationResponse register(UserRegistrationRequest request);
    UserLoginResponse login(UserLoginRequest request);

    //CRUD
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Integer userId);
    UserResponse getUserByEmail(String email);
    void deleteUser(Integer userId);
    UserResponse updateUser(Integer userID, User user);
    void updatePassword(String email, String newPassword);

}
