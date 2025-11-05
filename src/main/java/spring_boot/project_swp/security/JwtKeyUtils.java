package spring_boot.project_swp.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public final class JwtKeyUtils {
  private JwtKeyUtils() {}

  public static Key hmacKey(String secret) {
    // Try Base64 decode first; if it fails, fall back to raw UTF-8 bytes
    try {
      byte[] decoded = Decoders.BASE64.decode(secret);
      return Keys.hmacShaKeyFor(decoded);
    } catch (IllegalArgumentException ex) {
      return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
  }
}
