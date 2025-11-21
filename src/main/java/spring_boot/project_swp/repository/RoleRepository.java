package spring_boot.project_swp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> { // <--- SỬA Integer THÀNH Long
  Optional<Role> findByRoleName(String roleName);
}
