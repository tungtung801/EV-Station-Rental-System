package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.exception.ConflictException;

public interface PaymentService {
  PaymentResponse createPayment(PaymentRequest request) throws ConflictException;

  PaymentResponse findPaymentById(Long paymentId);

  List<PaymentResponse> getPaymentsByRentalId(Long rentalId);

  PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusEnum status);

  PaymentResponse findPaymentByTransactionCode(String transactionCode);

  Payment savePayment(Payment payment);

  PaymentResponse createDepositPayment(Booking booking, String userEmail, PaymentRequest request);

  PaymentResponse createFinalPayment(Long rentalId, String userEmail, PaymentRequest request);
}
