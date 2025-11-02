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
import spring_boot.project_swp.dto.request.ForgotPasswordRequest;
import spring_boot.project_swp.dto.request.ResetPasswordRequest;
import spring_boot.project_swp.dto.request.VerifyOtpRequest;
import spring_boot.project_swp.dto.response.MessageResponse;
import spring_boot.project_swp.service.ForgotPasswordService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Forgot Password APIs", description = "APIs for managing forgotten passwords")
public class ForgotPassController {

  final ForgotPasswordService forgotPasswordService;

  @PostMapping("/forgot-password")
  @Operation(
      summary = "Forgot password",
      description = "Sends an OTP to the user's email for password reset.")
  public ResponseEntity<MessageResponse> forgotPassword(
      @RequestBody @Valid ForgotPasswordRequest request) {
    forgotPasswordService.forgotPassword(request);
    return new ResponseEntity<>(
        MessageResponse.builder().message("OTP sent to your email address").build(), HttpStatus.OK);
  }

  @PostMapping("/verify-otp")
  @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the user's email.")
  public ResponseEntity<MessageResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
    forgotPasswordService.verifyOtp(request);
    return new ResponseEntity<>(
        MessageResponse.builder().message("OTP verified successfully").build(), HttpStatus.OK);
  }

  @PostMapping("/reset-password")
  @Operation(
      summary = "Reset password",
      description = "Resets the user's password after successful OTP verification.")
  public ResponseEntity<MessageResponse> resetPassword(
      @RequestBody @Valid ResetPasswordRequest request) {
    forgotPasswordService.resetPassword(request);
    return new ResponseEntity<>(
        MessageResponse.builder().message("Password updated successfully").build(), HttpStatus.OK);
  }
}
