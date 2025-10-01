package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.UserRegistrationDto;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class RegisterController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {


        User registeredUser = userService.registerUser(registrationDto);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", registeredUser.getUserId());
        response.put("fullName", registeredUser.getFullName());
        response.put("email", registeredUser.getEmail());
        response.put("phoneNumber", registeredUser.getPhoneNumber());
        response.put("active", registeredUser.getAccountStatus());
        response.put("createdAt", registeredUser.getCreatedAt());
        response.put("role", registeredUser.getRole().getRoleName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
