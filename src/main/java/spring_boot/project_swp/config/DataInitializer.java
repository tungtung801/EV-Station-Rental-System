package spring_boot.project_swp.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.repository.*;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataInitializer implements CommandLineRunner {

  final RoleRepository roleRepository;
  final UserRepository userRepository;
  final UserProfileRepository userProfileRepository;
  final LocationRepository locationRepository;
  final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    // 1. TẠO CÁC ROLE CƠ BẢN (Nếu database chưa có)
    if (roleRepository.count() == 0) {
      roleRepository.save(Role.builder().roleName("Admin").build());
      roleRepository.save(Role.builder().roleName("Staff").build());
      roleRepository.save(Role.builder().roleName("User").build());
    }

    // 2. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH (admin@gmail.com / 123456)
    if (!userRepository.existsByEmailOrPhoneNumber("admin@gmail.com", null)) {
      User admin = new User();
      admin.setFullName("System Admin");
      admin.setEmail("admin@gmail.com");
      admin.setPhoneNumber("0000000000");
      admin.setPassword(passwordEncoder.encode("123456")); // Password
      admin.setAccountStatus(true);

      // Gán quyền Admin
      Role adminRole = roleRepository.findByRoleName("Admin").orElseThrow();
      admin.setRole(adminRole);

      User savedAdmin = userRepository.save(admin);

      // Tạo Profile mặc định cho Admin (Status = VERIFIED)
      // Phải có cái này để tránh lỗi NullPointerException khi Admin đăng nhập
      UserProfile profile = new UserProfile();
      profile.setUser(savedAdmin);
      profile.setStatus(UserProfileStatusEnum.VERIFIED);
      userProfileRepository.save(profile);

      System.out.println(">>> ADMIN CREATED: admin@gmail.com / 123456");
    }

    // 3. TẠO DỮ LIỆU LOCATION MẪU (HCM, Hà Nội, Đà Nẵng)
//    if (locationRepository.count() == 0) {
//      // Thành phố Hồ Chí Minh
//      Location hcm = Location.builder()
//          .locationName("Thành phố Hồ Chí Minh")
//          .locationType("City")
//          .latitude(new BigDecimal("10.78"))
//          .longitude(new BigDecimal("106.70"))
//          .radius(new BigDecimal("50.00"))
//          .isActive(true)
//          .build();
//      locationRepository.save(hcm);
//
//      // Hà Nội
//      Location hanoi = Location.builder()
//          .locationName("Hà Nội")
//          .locationType("City")
//          .latitude(new BigDecimal("21.03"))
//          .longitude(new BigDecimal("105.85"))
//          .radius(new BigDecimal("50.00"))
//          .isActive(true)
//          .build();
//      locationRepository.save(hanoi);
//
//      // Đà Nẵng
//      Location danang = Location.builder()
//          .locationName("Đà Nẵng")
//          .locationType("City")
//          .latitude(new BigDecimal("16.07"))
//          .longitude(new BigDecimal("108.23"))
//          .radius(new BigDecimal("50.00"))
//          .isActive(true)
//          .build();
//      locationRepository.save(danang);
//
//      System.out.println(">>> LOCATIONS CREATED: HCM, Hà Nội, Đà Nẵng");
//    }
  }
}
