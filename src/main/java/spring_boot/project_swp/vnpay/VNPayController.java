package spring_boot.project_swp.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/vnpay")
public class VNPayController {

    // Giả sử bạn có một lớp Config để đọc các giá trị từ application.properties
    // @Value("${vnpay.tmnCode}") private String tmnCode;
    // @Value("${vnpay.hashSecret}") private String hashSecret;
    // ...

    @org.springframework.beans.factory.annotation.Value("${vnpay.tmnCode}")
    private String tmnCode;

    @org.springframework.beans.factory.annotation.Value("${vnpay.hashSecret}")
    private String hashSecret;

    @org.springframework.beans.factory.annotation.Value("${vnpay.url}") // Added ${} to properly reference property
    private String vnpayUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @GetMapping("/create_payment")
    public String createPayment(HttpServletRequest req, @RequestParam("amount") long amount) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amountInVND = amount * 100; // Số tiền phải nhân 100
        String bankCode = req.getParameter("bankCode"); // Lấy mã ngân hàng nếu có

        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        // Lấy địa chỉ IP một cách chính xác
        String vnp_IpAddr = VnpayUtils.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", this.tmnCode); // Sử dụng giá trị đã tiêm
        vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", this.returnUrl); // Sử dụng giá trị đã tiêm
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build data to hash and query string
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
        // Sử dụng giá trị hashSecret đã tiêm và gọi hàm từ lớp Utils
        String vnp_SecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return this.vnpayUrl + "?" + queryUrl;
    }
}


