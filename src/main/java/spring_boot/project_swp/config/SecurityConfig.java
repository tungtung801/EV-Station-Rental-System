package spring_boot.project_swp.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import spring_boot.project_swp.security.CustomAuthEntryPoint;
import spring_boot.project_swp.security.CustomUserDetailsService;
import spring_boot.project_swp.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;
  private final CustomAuthEntryPoint customAuthEntryPoint;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  // ✅ BƯỚC 1: THÊM BEAN CẤU HÌNH CORS
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Dùng allowedOriginPatterns để cho phép mọi subdomain của ngrok
    configuration.setAllowedOriginPatterns(
        List.of(
            "http://localhost:5173", // Cho phép Vite dev server
            "https://*.ngrok-free.app" // Cho phép bất kỳ URL nào của ngrok
            ));

    // Hoặc dùng allowedOrigins nếu bạn muốn chỉ định URL ngrok cụ thể
    // configuration.setAllowedOrigins(List.of(
    //         "http://localhost:5173",
    //         "https://5741c8de4ef9.ngrok-free.app"
    // ));

    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // Áp dụng cho tất cả các đường dẫn
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Áp dụng cấu hình CORS bạn vừa định nghĩa ở trên
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // Tắt CSRF nếu FE dùng React, Vue, Next
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthEntryPoint))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // các API không cần phân quyền
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/auth/**")
                    .permitAll()

                    // Cho phép truy cập public vào thư mục uploads để hiển thị ảnh
                    .requestMatchers("/uploads/**")
                    .permitAll()

                    // RoleController
                    .requestMatchers("/api/roles/**")
                    .hasAuthority("Admin")

                    // UserController (plural base path is /api/users)
                    .requestMatchers(
                        "/api/user/**") // NOTE: seems unused (singular); kept if legacy
                    .hasAnyAuthority("Admin", "User")
                    .requestMatchers(HttpMethod.POST, "/api/users/upload-image")
                    .hasAnyAuthority("User", "Staff", "Admin")
                    .requestMatchers(HttpMethod.GET, "/api/users/*")
                    .hasAnyAuthority("User", "Staff", "Admin")
                    .requestMatchers(HttpMethod.PUT, "/api/users/*")
                    .hasAnyAuthority("User", "Staff", "Admin")
                    .requestMatchers("/api/users/user", "/api/users/staff", "/api/users/delete/**")
                    .hasAnyAuthority("Admin", "Staff")

                    // UserProfileController
                    // Specific patterns BEFORE any broader multi-level patterns
                    .requestMatchers(
                        HttpMethod.GET, "/api/user-profiles", "/api/user-profiles/pending")
                    .hasAnyAuthority("Staff", "Admin")
                    .requestMatchers(
                        HttpMethod.GET, "/api/user-profiles/*", "/api/user-profiles/user/*")
                    .hasAnyAuthority("User", "Admin")
                    .requestMatchers(HttpMethod.GET, "/api/user-profiles/status/*")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers(HttpMethod.PUT, "/api/user-profiles")
                    .hasAnyAuthority("User", "Admin")
                    .requestMatchers(HttpMethod.PUT, "/api/user-profiles/*") // update by id
                    .hasAnyAuthority("User", "Admin")
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/api/user-profiles/*/approve",
                        "/api/user-profiles/*/reject")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/user-profiles/*")
                    .hasAuthority("Admin")

                    // VehicleModelController
                    .requestMatchers("/api/vehicle-models/**")
                    .hasAuthority("Admin")

                    // VehicleController - CHO PHÉP NGƯỜI DÙNG KHÔNG CẦN ĐĂNG NHẬP XEM VEHICLES
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/vehicles",
                        "/api/vehicles/*",
                        "/api/vehicles/*/active-bookings")
                    .permitAll()
                    .requestMatchers("/api/vehicles/**")
                    .hasAuthority("Admin")

                    // Vehicle ongoing bookings reused via booking controller mapping
                    .requestMatchers(HttpMethod.GET, "/api/bookings/vehicle/*/ongoing")
                    .hasAnyAuthority("Admin", "Staff", "User")

                    // StationController (reordered: allow GET first, then admin for mutations)
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/station",
                        "/api/station/location/*",
                        "/api/station/city/*",
                        "/api/station/city/*/district/*")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers("/api/station/**")
                    .hasAuthority("Admin")

                    // LocationController (reordered similarly)
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/location",
                        "/api/location/*",
                        "/api/location/getCities",
                        "/api/location/getDistricts/*",
                        "/api/location/getWards/*")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers("/api/location/**")
                    .hasAuthority("Admin")

                    // BookingController
                    .requestMatchers(HttpMethod.POST, "/api/bookings")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers(
                        HttpMethod.GET, "/api/bookings", "/api/bookings/*", "/api/bookings/user/*")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/bookings/count-completed-rentals",
                        "/api/bookings/user/count-completed-rentals/*")
                    .hasAuthority("Admin")
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel")
                    .hasAuthority("User")
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/*/confirm-deposit")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/status")
                    .hasAnyAuthority("Admin", "Staff", "User")

                    // RentalController
                    .requestMatchers(HttpMethod.POST, "/api/rentals")
                    .hasAuthority("User")
                    .requestMatchers(HttpMethod.GET, "/api/rentals", "/api/rentals/*")
                    .hasAnyAuthority("User", "Admin", "Staff")
                    .requestMatchers(
                        HttpMethod.GET, "/api/rentals/renter/*", "/api/rentals/vehicle/*")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.PUT, "/api/rentals/*")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/rentals/*")
                    .hasAuthority("Admin")

                    // RentalDiscount
                    .requestMatchers(HttpMethod.POST, "/api/rental-discounts")
                    .hasAuthority("Admin")
                    .requestMatchers(HttpMethod.DELETE, "/api/rental-discounts/*/*")
                    .hasAuthority("Admin")
                    .requestMatchers(HttpMethod.GET, "/api/rental-discounts")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/rental-discounts/rental/*",
                        "/api/rental-discounts/discount/*")
                    .hasAuthority("Admin")

                    // Incident Report
                    .requestMatchers(
                        HttpMethod.POST, "/api/incident-reports", "/api/incident-reports/*")
                    .hasAuthority("User")
                    .requestMatchers(HttpMethod.GET, "/api/incident-reports")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.PUT, "/api/incident-reports/*")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/incident-reports/*")
                    .hasAuthority("Admin")

                    // DiscountController
                    .requestMatchers("/api/discounts/**")
                    .hasAuthority("Admin")

                    // PaymentController (updated to reflect actual mappings)
                    .requestMatchers(HttpMethod.POST, "/api/payments")
                    .hasAuthority("User")
                    .requestMatchers(HttpMethod.PUT, "/api/payments/*/confirm")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.PATCH, "/api/payments/*/status")
                    .hasAuthority("Admin")
                    .requestMatchers(HttpMethod.GET, "/api/payments/*") // includes /{id}
                    .hasAnyAuthority("Admin", "Staff", "User")
                    .requestMatchers(HttpMethod.GET, "/api/payments")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.GET, "/api/payments/booking/*")
                    .hasAnyAuthority("Admin", "Staff", "User")
                    .requestMatchers("/vnpay_return")
                    .permitAll()

                    // VehicleCheck endpoints
                    .requestMatchers(HttpMethod.POST, "/api/vehiclechecks")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.GET, "/api/vehiclechecks", "/api/vehiclechecks/*")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.PUT, "/api/vehiclechecks/*")
                    .hasAnyAuthority("Admin", "Staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/vehiclechecks/*")
                    .hasAuthority("Admin")
                    .anyRequest()
                    .authenticated());

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
