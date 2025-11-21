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
      @RequestParam("bookingId") Long bookingId,
      @RequestParam("userId") Long userId)
      throws UnsupportedEncodingException {

    // 1. Lấy Booking
    BookingResponse booking = bookingService.getBookingById(bookingId);

    // 2. Lấy số tiền cần thanh toán (Trả Full)
    BigDecimal amount = booking.getTotalAmount();
    long amountInVND = amount.longValue() * 100; // VNPay yêu cầu nhân 100

    // 3. Tạo Payment PENDING trong Database trước
    PaymentRequest paymentRequest = new PaymentRequest();
    paymentRequest.setBookingId(bookingId);
    paymentRequest.setUserId(userId);
    paymentRequest.setAmount(amount);
    paymentRequest.setPaymentMethod(PaymentMethodEnum.VNPAY);
    paymentRequest.setPaymentType(PaymentTypeEnum.RENTAL_FEE);
    paymentRequest.setNote("Thanh toan Booking " + bookingId);

    PaymentResponse savedPayment = paymentService.createPayment(paymentRequest);

    // 4. Build URL VNPay
    String vnp_TxnRef = "PAY_" + savedPayment.getPaymentId() + "_" + System.currentTimeMillis();

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

    // Time
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
        hashData
            .append(fieldName)
            .append('=')
            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        query
            .append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
            .append('=')
            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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
