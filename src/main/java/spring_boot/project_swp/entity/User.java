package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UserId")
  Long userId;

  @Column(name = "FullName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
  String fullName;

  @Column(name = "Email", nullable = false, unique = true, length = 100)
  String email;

  @Column(name = "PhoneNumber", length = 20)
  String phoneNumber;

  @Column(name = "Password", nullable = false, length = 255)
  String password;

  @Column(name = "AccountStatus", nullable = false)
  @Builder.Default
  Boolean accountStatus = true; // True: Active, False: Locked

  // --- PHẦN KYC (Xác minh danh tính) ---
  @Column(name = "IsVerified", nullable = false)
  @Builder.Default
  Boolean isVerified = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VerifiedBy") // Người duyệt (Staff/Admin)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  User verifiedBy;

  @Column(name = "VerifiedAt")
  LocalDateTime verifiedAt;
  // ------------------------------------

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.EAGER) // Eager để tiện lấy Role khi Login
  @JoinColumn(name = "RoleId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Role role;

  // --- PHÂN BIỆT STAFF VÀ CUSTOMER ---

  // Station chỉ dành cho STAFF (Customer sẽ null)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "StationId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  Station station;

  // Lịch sử thuê xe chỉ dành cho CUSTOMER (Staff có thể null hoặc rỗng)
  @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  List<Rental> rentedRentals;

  // Việc làm của STAFF (Giao xe)
  @OneToMany(mappedBy = "pickupStaff", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  List<Rental> pickupStaffRentals;

  // Việc làm của STAFF (Nhận xe)
  @OneToMany(mappedBy = "returnStaff", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  List<Rental> returnStaffRentals;

  // Profile chi tiết (Bằng lái, CCCD...)
  @OneToOne(
      mappedBy = "user",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  UserProfile profile;
}
