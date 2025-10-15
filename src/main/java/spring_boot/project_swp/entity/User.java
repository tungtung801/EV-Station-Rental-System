package spring_boot.project_swp.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "FullName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PhoneNumber", nullable = false, unique = true, length = 20)
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
}
