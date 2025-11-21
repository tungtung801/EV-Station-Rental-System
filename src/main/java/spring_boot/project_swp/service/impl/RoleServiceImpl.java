package spring_boot.project_swp.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.repository.RoleRepository;
import spring_boot.project_swp.service.RoleService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleServiceImpl implements RoleService {

  final RoleRepository roleRepository;

  @Override
  public Role getRoleEntityByName(String roleName) {
    return roleRepository
        .findByRoleName(roleName)
        .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
  }
}
