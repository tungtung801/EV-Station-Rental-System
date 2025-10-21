package spring_boot.project_swp.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;

import spring_boot.project_swp.dto.response.UserProfileResponse;

import spring_boot.project_swp.dto.response.UserProfileVerificationResponse;
import spring_boot.project_swp.service.UserProfileService;

import java.util.List;

import spring_boot.project_swp.dto.request.UserProfileVerificationRequest;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileController {

    final UserProfileService userProfileService;

    //------------ Create UserProfile ----------
    @PostMapping
    public ResponseEntity<UserProfileResponse> createUserProfile(@ModelAttribute @Valid UserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.createUserProfile(request));
    }

    //------------ Get UserProfile ----------
    @GetMapping("/{profileId}")
    public ResponseEntity<UserProfileResponse> getUserProfileById(@PathVariable Integer profileId) {
        return ResponseEntity.ok(userProfileService.getUserProfileById(profileId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(userProfileService.getUserProfileByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllUserProfiles() {
        return ResponseEntity.ok(userProfileService.getAllUserProfiles());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserProfileResponse>> getAllPendingUserProfiles() {
        return ResponseEntity.ok(userProfileService.getAllPendingUserProfiles());
    }

    //------------ Update UserProfile ----------
    @PutMapping("/{profileId}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable Integer profileId, @ModelAttribute @Valid UserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(profileId, request));
    }

    //------------ Delete UserProfile ----------
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable Integer profileId) {
        userProfileService.deleteUserProfile(profileId);
        return ResponseEntity.noContent().build();
    }

    //------------ Verify/Reject UserProfile ----------
    @PutMapping("/verify-reject")
    public ResponseEntity<UserProfileVerificationResponse> verifyOrRejectUserProfile(@RequestBody @Valid UserProfileVerificationRequest request) {
        return ResponseEntity.ok(userProfileService.verifyOrRejectUserProfile(request));
    }
}