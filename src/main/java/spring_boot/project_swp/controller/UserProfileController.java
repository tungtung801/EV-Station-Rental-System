package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.UserProfileRejectionRequest;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.response.UserProfileResponse;
import spring_boot.project_swp.service.UserProfileService;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "User Profile APIs", description = "APIs for managing user profiles")
public class UserProfileController {

  final UserProfileService userProfileService;

  @GetMapping("/{profileId}")
  @Operation(summary = "Get user profile by ID")
  public ResponseEntity<UserProfileResponse> getUserProfileById(@PathVariable Long profileId) {
    return ResponseEntity.ok(userProfileService.getUserProfileById(profileId));
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get user profile by user ID")
  public ResponseEntity<UserProfileResponse> getUserProfileByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.getUserProfileByUserId(userId));
  }

  @GetMapping
  @Operation(summary = "Get all user profiles")
  public ResponseEntity<List<UserProfileResponse>> getAllUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllUserProfiles());
  }

  @GetMapping("/pending")
  @Operation(summary = "Get all pending user profiles")
  public ResponseEntity<List<UserProfileResponse>> getAllPendingUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllPendingUserProfiles());
  }

  @GetMapping("/verified")
  @Operation(summary = "Get all verified user profiles")
  public ResponseEntity<List<UserProfileResponse>> getAllVerifiedUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllVerifiedUserProfiles());
  }

  @GetMapping("/rejected")
  @Operation(summary = "Get all rejected user profiles")
  public ResponseEntity<List<UserProfileResponse>> getAllRejectedUserProfiles() {
    return ResponseEntity.ok(userProfileService.getAllRejectedUserProfiles());
  }

  @PutMapping
  @Operation(
      summary = "Update user profile (and Upload Docs)",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = "multipart/form-data",
                      schema = @Schema(implementation = UserProfileRequest.class))))
  public ResponseEntity<UserProfileResponse> updateUserProfile(
      @ModelAttribute @Valid UserProfileRequest request) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    Long userId = userProfileService.getUserIdByEmail(userEmail);

    return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
  }

  @DeleteMapping("/{profileId}")
  @Operation(summary = "Delete user profile")
  public ResponseEntity<Void> deleteUserProfile(@PathVariable Long profileId) {
    userProfileService.deleteUserProfile(profileId);
    return ResponseEntity.noContent().build();
  }

  // ĐÃ XÓA uploadVerificationDocuments

  @GetMapping("/status/{userId}")
  @Operation(summary = "Get user profile status")
  public ResponseEntity<UserProfileResponse> getUserProfileStatus(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.getUserProfileStatus(userId));
  }

  @PutMapping("/{userId}/approve")
  @Operation(summary = "Approve user profile")
  public ResponseEntity<UserProfileResponse> approveUserProfile(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.approveUserProfile(userId));
  }

  @PutMapping("/{userId}/reject")
  @Operation(summary = "Reject user profile")
  public ResponseEntity<UserProfileResponse> rejectUserProfile(
      @PathVariable Long userId, @RequestBody @Valid UserProfileRejectionRequest request) {
    return ResponseEntity.ok(userProfileService.rejectUserProfile(userId, request));
  }
}
