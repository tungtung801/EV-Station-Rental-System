package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.ForgotPasswordRequest;
import spring_boot.project_swp.dto.request.ResetPasswordRequest;
import spring_boot.project_swp.dto.request.VerifyOtpRequest;

public interface ForgotPasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
}