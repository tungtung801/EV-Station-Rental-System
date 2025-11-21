package spring_boot.project_swp.service;

import spring_boot.project_swp.entity.Role;

public interface RoleService {
  Role getRoleEntityByName(String roleName);
}
