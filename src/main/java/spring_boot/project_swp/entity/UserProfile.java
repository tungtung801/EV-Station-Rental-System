package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "UserProfiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ProfileId")
  Long profileId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserId", nullable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  User user;

  @Column(name = "DrivingLicenseUrl", length = 255)
  String drivingLicenseUrl;

  @Column(name = "IdCardUrl", length = 255)
  String idCardUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", nullable = false, length = 50)
          @Builder.Default
  UserProfileStatusEnum status =UserProfileStatusEnum.UNVERIFIED;

  @Column(name = "Reason", columnDefinition = "NVARCHAR(255)")
  String reason;

  @Column(name = "Bio", columnDefinition = "NVARCHAR(500)")
  String bio;
}
