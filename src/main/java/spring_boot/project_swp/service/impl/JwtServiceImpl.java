package spring_boot.project_swp.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.service.JwtService;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtServiceImpl implements JwtService {

  @Value("${application.security.jwt.secret-key}")
  String secretKey;

  @Value("${application.security.jwt.expiration}")
  long jwtExpiration;

  @Value("${application.security.jwt.refresh-token.expiration}")
  long refreshExpiration;

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  @Override
  public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public String generateToken(User user) {
    return generateToken(java.util.Collections.emptyMap(), user);
  }

  public String generateToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, jwtExpiration);
  }

  @Override
  public String generateRefreshToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, refreshExpiration);
  }

  private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(user.getEmail())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey()) // Cú pháp mới
        .compact();
  }

  @Override
  public boolean isTokenValid(String token, User user) {
    final String userName = extractUserName(token);
    return (userName.equals(user.getEmail())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser() // Cú pháp mới 0.12.3
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      // Key phải lấy từ hàm getSignInKey()
      io.jsonwebtoken.Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }
}
