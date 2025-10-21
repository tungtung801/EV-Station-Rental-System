package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import spring_boot.project_swp.entity.User;

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
    private int profileId;
    @Column(name = "LicenseNumberImage", nullable = false)
    private String licenseNumberImage;
    @Column(name = "IdCardImage", nullable = false)
    private String idCardImage;
    @Column(name = "Status", nullable = false, length = 50)
    private String status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;
}
