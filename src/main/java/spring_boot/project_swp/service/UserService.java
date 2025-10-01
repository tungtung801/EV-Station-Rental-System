package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.UserRegistrationDto;
import spring_boot.project_swp.entity.User;

import java.util.List;

@Service
public interface UserService {
    List<User> getAllUsers();

    User registerUser(UserRegistrationDto registrationDto);

    User getUserByEmailAndPassword(String email, String password);

    User getUserByEmail(String email);

    User getUserById(int id);

    User getUserbyPhoneNumber(String phoneNumber);

    User createUser(User user);

    User updateUser(int userId, User user);

    void deleteUser(int id);
}
