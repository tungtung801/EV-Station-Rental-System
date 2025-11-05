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
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.DocumentUploadRequest;
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
      description = "Updates an existing user profile's information.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = "multipart/form-data",
                      schema = @Schema(implementation = UserProfileRequest.class))))
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

  @PostMapping("/upload-verification-documents/{userId}")
  @Operation(
      summary = "Upload verification documents",
      description = "Uploads CCCD and GPLX documents for user verification.")
  public ResponseEntity<Void> uploadVerificationDocuments(
      @PathVariable Long userId, @ModelAttribute @Valid DocumentUploadRequest request) {
    userProfileService.uploadVerificationDocuments(userId, request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/status/{userId}")
  @Operation(
      summary = "Get user profile status",
      description = "Retrieves the status of a user's profile.")
  public ResponseEntity<UserProfileResponse> getUserProfileStatus(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.getUserProfileStatus(userId));
  }

  @PutMapping("/{userId}/approve")
  @Operation(
      summary = "Approve user profile",
      description = "Approves a user profile verification request.")
  public ResponseEntity<UserProfileResponse> approveUserProfile(@PathVariable Long userId) {
    return ResponseEntity.ok(userProfileService.approveUserProfile(userId));
  }

  @PutMapping("/{userId}/reject")
  @Operation(
      summary = "Reject user profile",
      description = "Rejects a user profile verification request.")
  public ResponseEntity<UserProfileResponse> rejectUserProfile(
      @PathVariable Long userId, @RequestBody @Valid UserProfileRejectionRequest request) {
    return ResponseEntity.ok(userProfileService.rejectUserProfile(userId, request));
  }
}
