package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;

import java.util.List;

@Service
public interface PaymentService {

    public List<PaymentResponse> findAllPaymentsStatus();

    public List<PaymentResponse> findAllPaymentsMethod(String status);

    public Payment findPaymentById(int paymentId);

    public PaymentResponse createPayment(Payment payment);

    public Payment UpdatePayment(Payment payment);

    public Payment updatePaymentStatus(int paymentId, String status);

    public Payment cancelPayment(int paymentId);

    public Payment confirmPayment(int paymentId);

    public Payment findPaymentByTransactionCode(String transactionCode);
}
