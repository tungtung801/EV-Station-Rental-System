package spring_boot.project_swp.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnpayUtils {

  /** Generate HMAC-SHA512 signature */
  public static String hmacSHA512(final String key, final String data) {
    try {
      if (key == null || data == null) {
        throw new NullPointerException();
      }
      final Mac hmac512 = Mac.getInstance("HmacSHA512");
      byte[] hmacKeyBytes = key.getBytes();
      final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
      hmac512.init(secretKey);
      byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
      byte[] res = hmac512.doFinal(dataBytes);
      StringBuilder sb = new StringBuilder(2 * res.length);
      for (byte b : res) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception ex) {
      throw new RuntimeException("Error creating HMAC-SHA512 signature", ex);
    }
  }

  /** Get client IP address */
  public static String getIpAddress(HttpServletRequest request) {
    String ipAddress;
    try {
      ipAddress = request.getHeader("X-FORWARDED-FOR");
      if (ipAddress == null) {
        ipAddress = request.getRemoteAddr();
      }
    } catch (Exception e) {
      ipAddress = "Invalid IP address:" + e.getMessage();
    }
    return ipAddress;
  }
}
