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
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // --- 1. CÁC BEAN CƠ BẢN ---
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

    // --- 2. CẤU HÌNH CORS (QUAN TRỌNG: Hỗ trợ Localhost + Ngrok) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép Frontend Local và Ngrok (để demo)
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:5173",      // Vite React
                        "http://localhost:3000",      // Create React App
                        "https://*.ngrok-free.app",   // Ngrok Public URL
                        "https://*.ngrok-free.dev"
                ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*")); // Cho phép mọi Header (Authorization, Content-Type...)
        configuration.setAllowCredentials(true); // Cho phép gửi Cookie/Auth Header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // --- 3. CẤU HÌNH FILTER CHAIN (PHÂN QUYỀN CHI TIẾT) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Kích hoạt CORS từ Bean bên trên
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Tắt CSRF (Vì dùng JWT stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Xử lý lỗi 401 Unauthorized (trả về JSON thay vì HTML mặc định)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthEntryPoint))

                // Không dùng Session (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // --- PHÂN QUYỀN URL (AUTHORIZATION) ---
                .authorizeHttpRequests(auth -> auth

                        // 1. PUBLIC ENDPOINTS (Ai cũng vào được)
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", // Swagger Docs
                                "/api/auth/**",           // Login, Register
                                "/uploads/**",            // Xem ảnh (Static resources)
                                "/vnpay_return"           // Return URL thanh toán (sau này dùng)
                        ).permitAll()

                        // 2. MODULE USER (Quản lý người dùng)
                        // - Tạo Staff: Chỉ Admin được làm
                        .requestMatchers(HttpMethod.POST, "/api/users/staff").hasAuthority("Admin")
                        // - Xóa User: Chỉ Admin
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("Admin")
                        // - Xem danh sách User/Staff: Admin và Staff
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/staff").hasAnyAuthority("Admin", "Staff")
                        // - Xem chi tiết/Sửa user: Chính chủ (User), Staff hoặc Admin đều được
                        .requestMatchers("/api/users/**").hasAnyAuthority("User", "Staff", "Admin")

                        // 3. MODULE USER PROFILE (Hồ sơ lái xe)
                        // - Xem danh sách chờ duyệt (Pending): Admin và Staff (để duyệt)
                        .requestMatchers("/api/user-profiles/pending").hasAnyAuthority("Admin", "Staff")
                        // - Duyệt (Approve) hoặc Từ chối (Reject): Admin và Staff
                        .requestMatchers(HttpMethod.PUT, "/api/user-profiles/*/approve", "/api/user-profiles/*/reject")
                        .hasAnyAuthority("Admin", "Staff")
                        // - User tự update hồ sơ của mình (Up bằng lái, CCCD)
                        .requestMatchers(HttpMethod.PUT, "/api/user-profiles").hasAnyAuthority("User", "Admin")
                        // - Xem chi tiết hồ sơ: Ai có quyền đăng nhập cũng xem được (để check)
                        .requestMatchers("/api/user-profiles/**").hasAnyAuthority("User", "Staff", "Admin")

                        // 4. MODULE ROLES (Chỉ Admin đụng vào)
                        .requestMatchers("/api/roles/**").hasAuthority("Admin")

                        // 5. MODULE XE (VEHICLES)
                        // - Khách xem xe (List, Search, Detail): Public
                        .requestMatchers(HttpMethod.GET,
                                "/api/vehicles",          // <--- THÊM CÁI NÀY (Get All)
                                "/api/vehicles/search",   // Tìm kiếm
                                "/api/vehicles/{id}"      // Xem chi tiết
                        ).permitAll()

                        // - Thêm/Sửa/Xóa xe: Chỉ Admin và Staff
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyAuthority("Admin", "Staff")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyAuthority("Admin", "Staff")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasAuthority("Admin")

                        // 6. MODULE ĐỊA ĐIỂM (STATION/LOCATION)
                        .requestMatchers(HttpMethod.GET, "/api/station/**", "/api/location/**").permitAll() // Xem trạm thì ai cũng xem được
                        .requestMatchers("/api/station/**", "/api/location/**").hasAuthority("Admin") // Sửa đổi thì chỉ Admin

                        // 7. MODULE BOOKING (ĐẶT XE) - Quan trọng
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasAuthority("User") // Chỉ khách được đặt
                        .requestMatchers(HttpMethod.GET, "/api/bookings/my-bookings").hasAuthority("User") // Khách xem đơn mình
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").hasAnyAuthority("Admin", "Staff") // Admin xem hết

                        // --- 8. MODULE RENTAL (QUAN TRỌNG: Vận hành) ---
                        // Giao xe và Nhận xe: Chỉ Staff và Admin được làm
                        .requestMatchers("/api/rentals/**").hasAnyAuthority("Admin", "Staff")

                        // --- 9. MODULE PAYMENT (Thanh toán bổ sung) ---
                        // Tạo thanh toán: Khách (trả tiền thuê), Staff (thu tiền phạt)
                        .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyAuthority("User", "Staff", "Admin")

                        // --- 10. MODULE DISCOUNT (Khuyến mãi) ---
                        // Xem mã: Ai đăng nhập cũng xem được (để chọn mã)
                        .requestMatchers(HttpMethod.GET, "/api/discounts/**").authenticated()
                        // Tạo/Sửa/Xóa mã: Chỉ Admin
                        .requestMatchers("/api/discounts/**").hasAuthority("Admin")

                        // --- 11. END ---
                        .anyRequest().authenticated()
                );

        // Thêm Filter JWT vào trước Filter xác thực gốc của Spring
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Cấu hình Authentication Provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}