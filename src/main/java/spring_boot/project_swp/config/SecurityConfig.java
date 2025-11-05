package spring_boot.project_swp.config;

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
import spring_boot.project_swp.security.CustomAuthEntryPoint;
import spring_boot.project_swp.security.CustomFailureHandler;
import spring_boot.project_swp.security.CustomSuccessHandler;
import spring_boot.project_swp.security.CustomUserDetailsService;
import spring_boot.project_swp.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;
  private final CustomSuccessHandler customSuccessHandler;
  private final CustomFailureHandler customFailureHandler;
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

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
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

                    // RoleController
                    .requestMatchers("/api/roles/**")
                    .hasAuthority("admin")

                    // UserController
                    .requestMatchers("/api/user/**")
                    .hasAnyAuthority("admin", "user")
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}")
                    .hasAnyAuthority("user", "staff", "admin")
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}")
                    .hasAnyAuthority("user", "staff", "admin")
                    .requestMatchers("/api/users/user", "/api/users/staff", "/api/users/delete/**")
                    .hasAnyAuthority("admin", "staff")

                    // UserProfileController
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/user-profiles/{profileId}",
                        "/api/user-profiles/user/{userId}")
                    .hasAnyAuthority("user", "admin")
                    // Only GET list endpoints require staff/admin; do not shadow PUT
                    .requestMatchers(
                        HttpMethod.GET, "/api/user-profiles", "/api/user-profiles/pending")
                    .hasAnyAuthority("staff", "admin")
                    .requestMatchers(HttpMethod.PUT, "/api/user-profiles")
                    .hasAnyAuthority("user", "admin")
                    .requestMatchers(HttpMethod.PUT, "/api/user-profiles/{userId}")
                    .hasAnyAuthority("user", "admin")
                    .requestMatchers(HttpMethod.DELETE, "/api/user-profiles/{profileId}")
                    .hasAuthority("admin")

                    // VehicleModelController
                    .requestMatchers("/api/vehicle-models/**")
                    .hasAuthority("admin")

                    // VehicleController
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/vehicles",
                        "/api/vehicles/{id}",
                        "/api/vehicles/{vehicleId}/active-bookings")
                    .hasAnyAuthority("user", "admin", "staff")
                    .requestMatchers("/api/vehicles/**")
                    .hasAuthority("admin")

                    // StationController
                    .requestMatchers("/api/station/**")
                    .hasAuthority("admin")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/station",
                        "/api/station/location/{locationId}",
                        "/api/station/city/{cityId}",
                        "/api/station/city/{cityId}/district/{districtId}")
                    .hasAnyAuthority("user", "admin", "staff")

                    // LocationController
                    .requestMatchers("/api/location/**")
                    .hasAuthority("admin")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/location",
                        "/api/location/{locationId}",
                        "/api/location/getCities",
                        "/api/location/getDistricts/{cityId}",
                        "/api/location/getWards/{districtId}")
                    .hasAnyAuthority("user", "admin", "staff")

                    // BookingController
                    .requestMatchers(HttpMethod.POST, "/api/bookings")
                    .hasAnyAuthority("user", "admin", "staff")
                    .requestMatchers("/api/bookings/**")
                    .hasAuthority("admin")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/bookings",
                        "/api/bookings/{bookingId}",
                        "/api/bookings/user/{userId}")
                    .hasAnyAuthority("user", "admin", "staff")
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/{bookingId}/cancel")
                    .hasAuthority("user")
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/{bookingId}/confirm")
                    .hasAnyAuthority("admin", "staff")

                    // RentalController
                    .requestMatchers(HttpMethod.POST, "/api/rentals")
                    .hasAuthority("user")
                    .requestMatchers(HttpMethod.GET, "/api/rentals", "/api/rentals/{rentalId}")
                    .hasAnyAuthority("user", "admin", "staff")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/rentals/renter/{renterId}",
                        "/api/rentals/vehicle/{vehicleId}")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.PUT, "/api/rentals/{rentalId}")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/rentals/{rentalId}")
                    .hasAuthority("admin")

                    // RentalDiscount
                    .requestMatchers(HttpMethod.POST, "/api/rental-discounts")
                    .hasAuthority("admin")
                    .requestMatchers(
                        HttpMethod.DELETE, "/api/rental-discounts/{rentalId}/{discountId}")
                    .hasAuthority("admin")
                    .requestMatchers(HttpMethod.GET, "/api/rental-discounts")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/rental-discounts/rental/{rentalId}",
                        "/api/rental-discounts/discount/{discountId}")
                    .hasAuthority("admin")

                    // Incident Report
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/incident-reports",
                        "/api/incident-reports/{reportId}")
                    .hasAuthority("user")
                    .requestMatchers(HttpMethod.GET, "/api/incident-reports")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.PUT, "/api/incident-reports/{reportId}")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.DELETE, "/api/incident-reports/{reportId}")
                    .hasAuthority("admin")

                    // DiscountController
                    .requestMatchers("/api/discounts/**")
                    .hasAuthority("admin")

                    // PaymentController
                    .requestMatchers(HttpMethod.POST, "/api/payments/createpayment")
                    .hasAuthority("user")
                    .requestMatchers(
                        HttpMethod.POST, "/api/payments/updatepayment/{paymentId}/{status}")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.GET, "/api/payments/{paymentId}")
                    .hasAnyAuthority("admin", "staff")
                    .requestMatchers(HttpMethod.GET, "/api/payments/transaction/{transactionCode}")
                    .hasAnyAuthority("admin", "staff", "user")
                    .anyRequest()
                    .authenticated());

    http.authenticationProvider(authenticationProvider());

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
