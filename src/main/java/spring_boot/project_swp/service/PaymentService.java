package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;

public interface PaymentService {
  // Tạo thanh toán (Cho cả Online và Offline)
  PaymentResponse createPayment(PaymentRequest request);

  // Xác nhận đã nhận tiền (Staff dùng cho Offline)
  PaymentResponse confirmPayment(Long paymentId, Long staffId);

  // Update trạng thái (Dùng cho IPN VNPay hoặc khi hủy/hoàn tiền)
  PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request);

  List<PaymentResponse> getPaymentsByBookingId(Long bookingId);

  List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long paymentId);
}
