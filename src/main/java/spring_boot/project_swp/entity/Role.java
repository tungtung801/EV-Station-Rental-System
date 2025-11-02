package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "RoleId")
  private Long roleId;

  @Column(name = "RoleName", nullable = false, unique = true, length = 50)
  private String roleName;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  @ToString.Exclude
  @JsonIgnore
  @Builder.Default
  private List<User> users = new ArrayList<>();
}
