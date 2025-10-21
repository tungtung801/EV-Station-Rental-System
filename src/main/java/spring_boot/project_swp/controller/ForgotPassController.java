package spring_boot.project_swp.controller;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPassController {

    final ForgotPasswordService forgotPasswordService;

    //------------ Forgot Password ----------
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        forgotPasswordService.forgotPassword(request);
        return new ResponseEntity<>(MessageResponse.builder().message("OTP sent to your email address").build(), HttpStatus.OK);
    }

    //------------ Verify OTP ----------
    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        forgotPasswordService.verifyOtp(request);
        return new ResponseEntity<>(MessageResponse.builder().message("OTP verified successfully").build(), HttpStatus.OK);
    }

    //------------ Reset Password ----------
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        forgotPasswordService.resetPassword(request);
        return new ResponseEntity<>(MessageResponse.builder().message("Password updated successfully").build(), HttpStatus.OK);
    }
}
