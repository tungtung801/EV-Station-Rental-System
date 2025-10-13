package spring_boot.project_swp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UserProfiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProfileId")
    private int profileId;
    @Column(name = "LiscenseNumberImage", nullable = false)
    private String liscenseNumberImage;
    @Column(name = "IdCardImage", nullable = false)
    private String idCardImage;
    @Column(name = "Status", nullable = false, length = 50)
    private String status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;
}
