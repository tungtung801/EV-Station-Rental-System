package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import spring_boot.project_swp.dto.request.user_request.UserLoginRequest;
import spring_boot.project_swp.dto.request.user_request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.user_response.UserLoginResponse;
import spring_boot.project_swp.dto.response.user_response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.user_response.UserResponse;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        UserResponse response = userService.getUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
