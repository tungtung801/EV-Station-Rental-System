package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.user_request.UserLoginRequest;
import spring_boot.project_swp.dto.request.user_request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.user_response.UserLoginResponse;
import spring_boot.project_swp.dto.response.user_response.UserRegistrationResponse;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.userRegistration(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
