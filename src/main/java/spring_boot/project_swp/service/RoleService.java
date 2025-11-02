package spring_boot.project_swp.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.dto.response.RoleResponse;
import spring_boot.project_swp.entity.Role;

@Service
public interface RoleService {
  List<RoleResponse> getAllRoles();

  Optional<RoleResponse> findByRoleName(String roleName);

  Role getRoleByName(String roleName);

  Optional<RoleResponse> findByRoleId(int roleId);

  RoleResponse createRole(RoleRequest request);

  RoleResponse updateRole(int roleId, RoleRequest request);

  void deleteRole(int roleId);

  RoleResponse getRoleById(int roleId);
}
