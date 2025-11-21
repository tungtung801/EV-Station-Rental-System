package spring_boot.project_swp.service;

import java.util.Map;
import spring_boot.project_swp.entity.User;

public interface JwtService {
  String generateToken(User user);

  String generateRefreshToken(Map<String, Object> extraClaims, User user);

  String extractUserName(String token);

  boolean isTokenValid(String token, User user);

  // THÊM HÀM NÀY (Để JwtAuthenticationFilter gọi)
  boolean validateToken(String token);
}
