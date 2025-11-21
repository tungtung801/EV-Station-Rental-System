package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.StaffRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "User APIs", description = "APIs for managing user accounts")
public class UserController {

  final UserService userService;
  final FileStorageService fileStorageService;

  @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload user image")
  public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
    String filename = fileStorageService.saveFile(file);
    return ResponseEntity.ok(filename);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user details")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    return new ResponseEntity<>(userService.updateUser(id, request), HttpStatus.OK);
  }

  @PostMapping("/staff")
  @Operation(summary = "Create a new staff account")
  public ResponseEntity<UserRegistrationResponse> createStaff(
      @Valid @RequestBody StaffRegistrationRequest request) {
    UserRegistrationResponse response = userService.createStaff(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/user")
  @Operation(summary = "Get all users")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
  }

  @GetMapping("/staff")
  @Operation(summary = "Get all staff members")
  public ResponseEntity<List<UserResponse>> getAllStaffs() {
    return new ResponseEntity<>(userService.getAllStaff(), HttpStatus.OK);
  }

  @DeleteMapping("/delete/{userId}")
  @Operation(summary = "Delete users with id")
  public ResponseEntity<?> deleteUserWithId(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
