package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfileStatusEnum;

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
    Integer profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @ToString.Exclude
    User user;

    @Column(name = "DrivingLicenseUrl", length = 255)
    String drivingLicenseUrl;

    @Column(name = "IdCardUrl", length = 255)
    String idCardUrl;

    @Column(name = "Status", nullable = true, length = 50)
    String status;

    @Column(name = "Reason", columnDefinition = "NTEXT")
    String reason;

    @Column(name = "Bio", columnDefinition = "NTEXT")
    String bio;

    @Column(name = "Preferences", columnDefinition = "NTEXT")
    String preferences;
}
