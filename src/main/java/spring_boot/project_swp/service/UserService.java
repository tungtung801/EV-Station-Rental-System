package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.user_request.UserLoginRequest;
import spring_boot.project_swp.dto.request.user_request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.user_response.UserLoginResponse;
import spring_boot.project_swp.dto.response.user_response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.user_response.UserResponse;
import spring_boot.project_swp.entity.User;

import java.util.List;


public interface UserService {
    //Authentication
    UserRegistrationResponse userRegistration(UserRegistrationRequest request);
    UserLoginResponse login(UserLoginRequest request);

    //CRUD
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Integer userId);
    User getUserByEmail(String email);
    void deleteUser(Integer userId);
    UserResponse updateUser(Integer userID, User user);

}
