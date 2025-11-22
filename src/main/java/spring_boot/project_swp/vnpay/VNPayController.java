package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequestMapping("/vnpay")
@RequiredArgsConstructor
@Tag(name = "VNPay APIs")
public class VNPayController {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    final PaymentService paymentService;
    final BookingService bookingService;

    @GetMapping("/create_payment")
    @Operation(summary = "Create VNPay payment URL")
    public String createPayment(
            HttpServletRequest req,
            @RequestParam("bookingId") Long bookingId)
            throws UnsupportedEncodingException {

        // 1. Lấy Booking
        BookingResponse booking = bookingService.getBookingById(bookingId);
        BigDecimal amount = booking.getTotalAmount();
        long amountInVND = amount.longValue() * 100;

        // --- LOGIC MỚI: KIỂM TRA XEM CÓ PAYMENT PENDING NÀO KHÔNG ---
        List<PaymentResponse> existingPayments = paymentService.getPaymentsByBookingId(bookingId);
        Long paymentIdToUse = null;

        // Tìm xem có cái nào đang PENDING và là VNPAY không
        for (PaymentResponse p : existingPayments) {
            if (p.getStatus() == PaymentStatusEnum.PENDING && p.getPaymentMethod() == PaymentMethodEnum.VNPAY) {
                paymentIdToUse = p.getPaymentId();
                break; // Tái sử dụng ngay
            }
        }

        // Nếu chưa có thì mới tạo mới
        if (paymentIdToUse == null) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setAmount(amount);
            paymentRequest.setPaymentMethod(PaymentMethodEnum.VNPAY);
            paymentRequest.setPaymentType(PaymentTypeEnum.RENTAL_FEE);
            paymentRequest.setNote("Thanh toan Booking " + bookingId);

            // Service tự lấy User từ Token
            PaymentResponse savedPayment = paymentService.createPayment(paymentRequest);
            paymentIdToUse = savedPayment.getPaymentId();
        }
        // ------------------------------------------------------------

        // 4. Build URL VNPay (Dùng paymentIdToUse)
        String vnp_TxnRef = "PAY_" + paymentIdToUse + "_" + System.currentTimeMillis();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan Booking:" + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", VnpayUtils.getIpAddress(req));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // Hash & Build Query
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());
                hashData.append(encodedValue);
                query.append(encodedValue);
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnpayUtils.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnpayUrl + "?" + queryUrl;
    }
}