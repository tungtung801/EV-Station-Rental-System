package spring_boot.project_swp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.user_request.UserLoginRequest;
import spring_boot.project_swp.dto.request.user_request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.user_response.UserLoginResponse;
import spring_boot.project_swp.dto.response.user_response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.user_response.UserResponse;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.exception.Print_Exception.ConflictException;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.RoleService;
import spring_boot.project_swp.service.UserService;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Override
    public UserRegistrationResponse userRegistration(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone number already in use");
        }
        User user = userMapper.toUser(request);
        user.setPassword(request.getPassword());
        user.setRole(roleService.getRoleByName("User"));
        User saved = userRepository.save(user);
        return UserRegistrationResponse.builder()
                .message("Registration successful")
                .userId(saved.getUserId())
                .email(saved.getEmail())
                .build();
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
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException("User not found with id: " + userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("User not found with email: " + email));
        return user;
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ConflictException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserResponse updateUser(Integer userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException("User not found with id: " + userId));

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber())) {
            throw new ConflictException("Phone number already in use");
        }

        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
        if (updatedUser.getFullName() != null) user.setFullName(updatedUser.getFullName());
        if (updatedUser.getPassword() != null) user.setPassword(updatedUser.getPassword());

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

}



