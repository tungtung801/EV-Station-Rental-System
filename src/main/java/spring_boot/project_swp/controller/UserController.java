package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.UserProfileService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User APIs", description = "APIs for managing user accounts and profiles")
public class UserController {
  UserService userService;
  private final UserProfileService userProfileService;
  private final FileStorageService fileStorageService;
  private final UserMapper userMapper;

  @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload user image", description = "Uploads a profile image for a user.")
  public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
    String filename = fileStorageService.saveFile(file);
    return ResponseEntity.ok(filename);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Retrieves a user's details by their unique ID.")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    UserResponse userResponse = userService.getUserById(id);
    return new ResponseEntity<>(userResponse, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update user details",
      description = "Updates an existing user's information.")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    UserResponse response = userService.updateUser(id, request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/staff")
  @Operation(
      summary = "Create a new staff account",
      description = "Registers a new staff member account.")
  public ResponseEntity<UserRegistrationResponse> createStaff(
      @Valid @RequestBody UserRegistrationRequest request) {
    UserRegistrationResponse response = userService.registerStaff(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/user")
  @Operation(summary = "Get all users", description = "Retrieves a list of all registered users.")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
  }

  @GetMapping("/staff")
  @Operation(
      summary = "Get all staff members",
      description = "Retrieves a list of all staff members.")
  public ResponseEntity<List<UserResponse>> getAllStaffs() {
    return new ResponseEntity<>(userService.getAllStaff(), HttpStatus.OK);
  }
}
