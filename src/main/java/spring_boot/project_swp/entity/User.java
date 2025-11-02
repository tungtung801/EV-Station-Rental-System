package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
  private Long userId;

  @Column(name = "FullName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
  private String fullName;

  @Column(name = "Email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "PhoneNumber", length = 20)
  private String phoneNumber;

  @Column(name = "Password", nullable = false, length = 255)
  private String password;

  @Column(name = "AccountStatus", nullable = false)
  @Builder.Default
  private Boolean accountStatus = true;

  @CreationTimestamp
  @Column(name = "CreatedAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RoleId", nullable = false)
  @ToString.Exclude
  private Role role;

  public User(Long userId) {
    this.userId = userId;
  }

  @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<Rental> rentedRentals;

  @OneToMany(mappedBy = "pickupStaff", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<Rental> pickupStaffRentals;

  @OneToMany(mappedBy = "returnStaff", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<Rental> returnStaffRentals;

  @OneToOne(
      mappedBy = "user",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  UserProfile profile;
}
