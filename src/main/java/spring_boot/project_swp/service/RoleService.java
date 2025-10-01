package spring_boot.project_swp.service;

import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.Role;
import java.util.List;
import java.util.Optional;

@Service
public interface RoleService {
    List<Role> getAllRoles();

    Optional<Role> findByRoleName(String roleName);

    Role getRoleByName(String roleName);

    Optional<Role> findByRoleId(int roleId);

    Role createRole(Role role);

    boolean updateRole(int roleId, Role role);

    void deleteRole(Role role);
}

