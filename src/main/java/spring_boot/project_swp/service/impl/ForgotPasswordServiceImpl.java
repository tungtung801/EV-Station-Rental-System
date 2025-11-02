package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.ForgotPasswordRequest;
import spring_boot.project_swp.dto.request.ResetPasswordRequest;
import spring_boot.project_swp.dto.request.VerifyOtpRequest;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.EmailService;
import spring_boot.project_swp.service.ForgotPasswordService;
import spring_boot.project_swp.service.UserService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

  final UserRepository userRepository;
  final EmailService emailService;
  final UserService userService;

  // Using ConcurrentHashMap to temporarily store OTP
  // Key: email, Value: OtpData
  final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();

  private static final long OTP_EXPIRATION_MINUTES = 5;

  @Override
  public void forgotPassword(ForgotPasswordRequest request) {
    userRepository
        .findByEmail(request.getEmail())
        .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

    String otp = generateOtp();
    LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
    otpStorage.put(request.getEmail(), new OtpData(otp, expiryTime));

    String subject = "Your password reset OTP";
    String text =
        "Your OTP is: " + otp + ". This OTP will expire in " + OTP_EXPIRATION_MINUTES + " minutes.";
    emailService.sendEmail(request.getEmail(), subject, text);
  }

  @Override
  public void verifyOtp(VerifyOtpRequest request) {
    OtpData otpData = otpStorage.get(request.getEmail());

    if (otpData == null) {
      throw new NotFoundException("OTP_NOT_FOUND_OR_EXPIRED");
    }

    if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
      otpStorage.remove(request.getEmail());
      throw new ConflictException("OTP_EXPIRED");
    }

    if (!otpData.getOtp().equals(request.getOtp())) {
      throw new ConflictException("INVALID_OTP");
    }
    // OTP is valid, can delete OTP after successful verification or keep it for the reset password
    // step
    // otpStorage.remove(request.getEmail());
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    String email = findEmailByOtp(request.getOtp());
    if (email == null) {
      throw new NotFoundException("OTP_NOT_FOUND_OR_EXPIRED");
    }

    OtpData otpData = otpStorage.get(email);
    if (otpData == null || !otpData.getOtp().equals(request.getOtp())) {
      throw new ConflictException("INVALID_OTP");
    }

    userService.updatePassword(email, request.getNewPassword());

    otpStorage.remove(email); // Xóa OTP sau khi đặt lại mật khẩu thành công
  }

  private String generateOtp() {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000); // 6-digit OTP
    return String.valueOf(otp);
  }

  private String findEmailByOtp(String otp) {
    for (java.util.Map.Entry<String, OtpData> entry : otpStorage.entrySet()) {
      if (entry.getValue().getOtp().equals(otp)) {
        return entry.getKey();
      }
    }
    return null;
  }

  // Inner class to store OTP and expiry time
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  private static class OtpData {
    String otp;
    LocalDateTime expiryTime;
  }
}
