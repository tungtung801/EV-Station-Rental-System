package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;

import java.util.List;

@Service
public interface PaymentService {

    public List<PaymentResponse> findAllPaymentsStatus();

    public List<PaymentResponse> findAllPaymentsMethod(String status);

    public PaymentResponse findPaymentById(String paymentId);

    public PaymentResponse createPayment(Payment payment);

    public PaymentResponse updatePaymentStatus(String paymentId, String status);

    public PaymentResponse cancelPayment(String paymentId);

    public PaymentResponse confirmPayment(String paymentId);

    public PaymentResponse findPaymentByTransactionCode(String paymentId);
}
