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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/vnpay")
@Tag(name = "VNPay APIs", description = "APIs for VNPay integration")
public class VNPayController {

  @Value("${vnpay.tmnCode}")
  private String tmnCode;

  @Value("${vnpay.hashSecret}")
  private String hashSecret;

  @Value("${vnpay.url}")
  private String vnpayUrl;

  @Value("${vnpay.returnUrl}")
  private String returnUrl;

  // @Autowired private BookingService bookingService; // Xóa dòng này

  @Autowired private PaymentService paymentService;

  @Autowired private UserService userService;

  @Autowired @Lazy private BookingService bookingService;

  @Autowired private BookingMapper bookingMapper;

  @GetMapping("/create_payment")
  @Operation(
      summary = "Create VNPay payment URL for booking deposit",
      description = "Generates a URL for initiating a VNPay payment for a specific booking.")
  public String createPayment(
      HttpServletRequest req,
      @RequestParam("bookingId") Long bookingId,
      @RequestParam("userId") Long userId)
      throws UnsupportedEncodingException {

    // 1. Lấy thông tin booking
    BookingResponse booking = bookingService.getBookingById(bookingId);
    BigDecimal depositAmount =
        booking
            .getExpectedTotal()
            .multiply(booking.getDepositPercent())
            .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP);

    // Lấy userEmail từ userId
    UserResponse userResponse = userService.getUserById(userId);
    String userEmail = userResponse.getEmail();

    // 2. Tạo một thanh toán mới với trạng thái PENDING
    PaymentRequest paymentRequest = new PaymentRequest();
    paymentRequest.setBookingId(bookingId);
    paymentRequest.setUserId(userId);
    paymentRequest.setAmount(depositAmount);
    paymentRequest.setPaymentMethod(PaymentMethodEnum.BANK_TRANSFER); // Or get from request
    // vnp_TxnRef sẽ được tạo và gán bên trong createDepositPayment
    spring_boot.project_swp.dto.response.PaymentResponse paymentResponse =
        paymentService.createDepositPayment(
            bookingMapper.toBooking(booking), userEmail, paymentRequest);

    // 3. Chuẩn bị các tham số cho VNPay
    String vnp_Version = "2.1.0";
    String vnp_Command = "pay";
    String orderType = "other";
    long amountInVND = depositAmount.longValue() * 100;
    String bankCode = req.getParameter("bankCode");

    String vnp_TxnRef = paymentResponse.getTransactionCode(); // Lấy mã giao dịch đã tạo
    String vnp_IpAddr = VnpayUtils.getIpAddress(req);

    Map<String, String> vnp_Params = new HashMap<>();
    vnp_Params.put("vnp_Version", vnp_Version);
    vnp_Params.put("vnp_Command", vnp_Command);
    vnp_Params.put("vnp_TmnCode", this.tmnCode);
    vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
    vnp_Params.put("vnp_CurrCode", "VND");

    if (bankCode != null && !bankCode.isEmpty()) {
      vnp_Params.put("vnp_BankCode", bankCode);
    }

    vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
    vnp_Params.put("vnp_OrderInfo", "Thanh toan dat coc cho don hang: " + bookingId);
    vnp_Params.put("vnp_OrderType", orderType);
    vnp_Params.put("vnp_Locale", "vn");
    vnp_Params.put("vnp_ReturnUrl", this.returnUrl);
    vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

    Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    String vnp_CreateDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

    cld.add(Calendar.MINUTE, 15);
    String vnp_ExpireDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

    // 4. Build URL
    List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = vnp_Params.get(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        // Build hash data
        hashData.append(fieldName);
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        // Build query
        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
        query.append('=');
        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        if (itr.hasNext()) {
          query.append('&');
          hashData.append('&');
        }
      }
    }
    String queryUrl = query.toString();
    String vnp_SecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());
    queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

    return this.vnpayUrl + "?" + queryUrl;
  }
}
