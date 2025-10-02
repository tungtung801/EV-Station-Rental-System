package spring_boot.project_swp.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.service.EmailService;
import spring_boot.project_swp.service.UserService;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ForgotPassController {
    private final UserService userService;

    private final EmailService emailSender;

    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        private final String otp;
        private final long expiryTime;

        public OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public boolean isValid(String inputOtp) {
            return otp.equals(inputOtp) && System.currentTimeMillis() < expiryTime;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }

        User existingUser = userService.getUserByEmail(email);
        if (existingUser == null) {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }


        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);


        otpStorage.put(email, new OtpData(otp, System.currentTimeMillis() + 600000));


        String subject = "Password Reset OTP";
        String text = "Your OTP for password reset is: " + otp + "\n\nThis code will expire in 10 minutes.";
        emailSender.sendEmail(email, subject, text);

        return new ResponseEntity<>("OTP sent to your email address", HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {
            return new ResponseEntity<>("Email and OTP are required", HttpStatus.BAD_REQUEST);
        }

        OtpData otpData = otpStorage.get(email);
        if (otpData == null) {
            return new ResponseEntity<>("No OTP request found", HttpStatus.BAD_REQUEST);
        }

        if (!otpData.isValid(otp)) {
            return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("OTP verified successfully", HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (email == null || otp == null || newPassword == null) {
            return new ResponseEntity<>("Email, OTP, and new password are required", HttpStatus.BAD_REQUEST);
        }
        if (!newPassword.equals(confirmPassword)) {
            return new ResponseEntity<>("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }

        OtpData otpData = otpStorage.get(email);
        if (otpData == null || !otpData.isValid(otp)) {
            return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }


        user.setPassword(newPassword);

        userService.updateUser(user.getUserId(), user);


        otpStorage.remove(email);

        return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
    }


}
