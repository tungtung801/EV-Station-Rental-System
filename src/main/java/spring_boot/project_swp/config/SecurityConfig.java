package spring_boot.project_swp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import spring_boot.project_swp.security.CustomFailureHandler;
import spring_boot.project_swp.security.CustomSuccessHandler;
import spring_boot.project_swp.security.CustomUserDetailsService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF nếu FE dùng React, Vue, Next
                .csrf(AbstractHttpConfigurer::disable)

                // các API không cần phân quyền
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/auth/**"
                        ).permitAll()

                        //RoleController
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")


                        // UserController
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasAnyRole("USER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasAnyRole("USER", "STAFF")


                        .requestMatchers( "/api/users/user", "/api/users/staff", "/api/users/delete/**").hasRole( "STAFF")


                        //UserProfileController
                        .requestMatchers(HttpMethod.GET, "/api/user-profiles/{profileId}", "/api/user-profiles/user/{userId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user-profiles", "/api/user-profiles/pending", "/api/user-profiles/verify-reject").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user-profiles/{userId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user-profiles/{profileId}").hasRole("ADMIN")


                        //VehicleModelController
                        .requestMatchers("/api/vehicle-models/**").hasRole("ADMIN")

                        //VehicleController
                        .requestMatchers("/api/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles", "/api/vehicles/{id}", "/api/vehicles/{vehicleId}/active-bookings").hasAnyRole("USER", "ADMIN", "STAFF")

                        //StationController
                        .requestMatchers("/api/station/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/station", "/api/station/location/{locationId}", "/api/station/city/{cityId}", "/api/station/city/{cityId}/district/{districtId}").hasAnyRole("USER", "ADMIN", "STAFF")

                        //LocationController
                        .requestMatchers("/api/location/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/location", "/api/location/{locationId}","/api/location/getCities", "/api/location/getDistricts/{cityId}", "/api/location/getWards/{districtId}").hasAnyRole("USER", "ADMIN", "STAFF")

                        //BookingController
                        .requestMatchers("/api/bookings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/bookings", "/api/bookings/{bookingId}", "/api/bookings/user/{userId}")
                        .hasAnyRole("USER", "ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/{bookingId}/cancel")
                        .hasAnyRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/{bookingId}/confirm").hasAnyRole("ADMIN", "STAFF")

                        //RentalController
                        .requestMatchers(HttpMethod.POST, "/api/rentals").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/rentals", "/api/rentals/{rentalId}").hasAnyRole("USER", "ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/rentals/renter/{renterId}", "/api/rentals/vehicle/{vehicleId}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/rentals/{rentalId}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/rentals/{rentalId}").hasAnyRole("ADMIN")

                        //RentalDiscount
                        .requestMatchers(HttpMethod.POST, "/api/rental-discounts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rental-discounts/{rentalId}/{discountId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/rental-discounts").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/rental-discounts/rental/{rentalId}", "/api/rental-discounts/discount/{discountId}").hasRole("ADMIN")

                        //Incident Report
                        .requestMatchers(HttpMethod.POST, "/api/incident-reports", "/api/incident-reports/{reportId}").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/incident-reports").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/incident-reports/{reportId}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/incident-reports/{reportId}").hasRole("ADMIN")

                        //DiscountController
                        .requestMatchers("/api/discounts/**").hasRole("ADMIN")


                        //PaymentController
                        .requestMatchers(HttpMethod.POST, "/api/payments/createpayment").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/payments/updatepayment/{paymentId}/{status}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{paymentId}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/payments/transaction/{transactionCode}").hasAnyRole("ADMIN", "STAFF", "USER")

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
