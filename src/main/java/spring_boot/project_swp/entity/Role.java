package spring_boot.project_swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*; // Dùng * cho gọn
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Roles")
@Data // Cẩn thận với cái này
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "RoleId")
  Long roleId; // Nên đồng nhất kiểu dữ liệu (Long)

  @Column(name = "RoleName", nullable = false, unique = true, length = 50)
  String roleName;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude // <--- THÊM CÁI NÀY để tránh lỗi HashCode
  @JsonIgnore
  @Builder.Default
  List<User> users = new ArrayList<>();
}
