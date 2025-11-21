package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Authentication APIs", description = "APIs for user authentication and registration")
public class AuthController {

  final UserService userService;

  @PostMapping("/register")
  @Operation(summary = "Register a new customer account")
  public ResponseEntity<UserRegistrationResponse> register(
      @Valid @RequestBody UserRegistrationRequest request) {
    UserRegistrationResponse response = userService.registerCustomer(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  @Operation(summary = "User login")
  public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
    UserLoginResponse response = userService.login(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
