package spring_boot.project_swp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

  @Value("${application.security.jwt.secret-key}")
  private String jwtSecret;

  @Value("${application.security.jwt.expiration}")
  private long jwtExpirationDate;

  @PostConstruct
  public void init() {
    log.info("JWT Secret Key: {}", jwtSecret);
    log.info("JWT Expiration Date: {}", jwtExpirationDate);
  }

  // generate JWT token
  public String generateToken(Authentication authentication) {
    String username = authentication.getName();

    Date currentDate = new Date();

    Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

    String token =
        Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(expireDate)
            .signWith(key())
            .compact();
    return token;
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  // get username from JWT token
  public String getUsername(String token) {
    Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  // validate JWT token
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(key()).build().parse(token);
      return true;
    } catch (MalformedJwtException e) {
      throw new JwtException("Invalid JWT token");
    } catch (ExpiredJwtException e) {
      throw new JwtException("Expired JWT token");
    } catch (UnsupportedJwtException e) {
      throw new JwtException("Unsupported JWT token");
    } catch (IllegalArgumentException e) {
      throw new JwtException("JWT claims string is empty");
    }
  }
}
