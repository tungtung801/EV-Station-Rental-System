package spring_boot.project_swp.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationDto;
import spring_boot.project_swp.dto.respone.ApiResponse;
import spring_boot.project_swp.dto.respone.UserLoginResponse;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleService roleService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmailAndPassword(String email, String password) {
        return userRepository.getUserByEmailAndPassword(email, password);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserbyPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User createUser(User user) {
        User existUser = getUserById(user.getUserId());
        if (existUser != null) {
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User updateUser(int userId, User user) {
        User existUser = getUserById(userId);
        if (existUser != null) {
            existUser.setEmail(user.getEmail());
            existUser.setPassword(user.getPassword());
            existUser.setRole(user.getRole());
            return userRepository.save(existUser);
        }
        return null;
    }

    @Override
    public void deleteUser(int id) {
        User existUser = getUserById(id);
        if (existUser != null) {
            userRepository.delete(existUser);
        }
    }

    @Override
    public User registerUser(UserRegistrationDto registrationDto) {

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        Role userRole = roleService.getRoleByName("user");

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());


        user.setRole(userRole);
        user.setAccountStatus("active");
        user.setCreatedAt(LocalDate.now());
        return userRepository.save(user);
    }

    @Override
    public ApiResponse<UserLoginResponse> login(UserLoginRequest request) {
        User existUser = userRepository.findByEmail(request.getEmail());
        if (existUser == null) {
            return ApiResponse.error("User not found");
        }

        if (!existUser.getPassword().equals(request.getPassword())) {
            return ApiResponse.error("Invalid password");
        }

        UserLoginResponse userResponse = new UserLoginResponse();

        userResponse.setUserId(existUser.getUserId());
        userResponse.setFullName(existUser.getFullName());
        userResponse.setEmail(existUser.getEmail());
        userResponse.setRoleName(existUser.getRole().getRoleName());
        return ApiResponse.success("Login successfully", userResponse);
    }
}
