package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;
import spring_boot.project_swp.service.UserProfileService;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "User Profile APIs", description = "APIs for managing user profiles")
public class UserProfileController {

  final UserProfileService userProfileService;

  // @PostMapping
  // public ResponseEntity<UserProfileResponse> createUserProfile(@ModelAttribute @Valid
  // UserProfileRequest request) {
  //     return ResponseEntity.ok(userProfileService.createUserProfile(request));
  // }

  @GetMapping("/{profileId}")
  @Operation(
      summary = "Get user profile by ID",
      description = "Retrieves a user profile by its unique ID.")
  public ResponseEntity<UserProfileResponse> getUserProfileById(@PathVariable Long profileId) {
    return ResponseEntity.ok(userProfileService.getUserProfileById(profileId));
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "Get user profile by user ID",
      description = "Retrieves a user profile by the associated user's ID.")
  public ResponseEntity<UserProfileResponse> getUserProfileByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.getUserProfileByUserId(userId));
  }

  @GetMapping
  @Operation(
      summary = "Get all user profiles",
      description = "Retrieves a list of all user profiles.")
  public ResponseEntity<List<UserProfileResponse>> getAllUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllUserProfiles());
  }

  @GetMapping("/pending")
  @Operation(
      summary = "Get all pending user profiles",
      description = "Retrieves a list of all user profiles awaiting verification.")
  public ResponseEntity<List<UserProfileResponse>> getAllPendingUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllPendingUserProfiles());
  }

  @PutMapping("/{userId}")
  @Operation(
      summary = "Update user profile",
      description = "Updates an existing user profile's information.")
  public ResponseEntity<UserProfileResponse> updateUserProfile(
      @PathVariable Long userId, @ModelAttribute @Valid UserProfileRequest request) {
    return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
  }

  @DeleteMapping("/{profileId}")
  @Operation(
      summary = "Delete user profile",
      description = "Deletes a user profile by its unique ID.")
  public ResponseEntity<Void> deleteUserProfile(@PathVariable Long profileId) {
    userProfileService.deleteUserProfile(profileId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/verify-reject")
  @Operation(
      summary = "Verify or reject user profile",
      description = "Verifies or rejects a user profile based on the provided request.")
  public ResponseEntity<UserProfileVerificationResponse> verifyOrRejectUserProfile(
      @RequestBody @Valid UserProfileVerificationRequest request) {
    return ResponseEntity.ok(userProfileService.verifyOrRejectUserProfile(request));
  }
}
