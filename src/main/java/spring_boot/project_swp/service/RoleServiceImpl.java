package spring_boot.project_swp.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.repository.RoleRepository;

import java.util.Optional;
import java.util.List;


@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public Optional<Role> findByRoleId(int roleId) {
        return roleRepository.findById(roleId);
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseThrow(() -> new IllegalStateException("Role not found"));
    }

    @Override
    public Role createRole(Role role) {
        if (roleRepository.findByRoleName(role.getRoleName()).isPresent()) {
            throw new IllegalArgumentException("Role already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public boolean updateRole(int roleId, Role role) {
//        Role existRole = findByRoleId(roleId);
//        if(existRole != null){
//            existRole.setRoleName(role.getRoleName());
//            return roleRepository.save(role) != null;
//        }
        return false;
    }

    @Override
    public void deleteRole(Role role) {
//        Role existRole = findByRoleId(role.getRoleId());
//        if(existRole != null){
//            roleRepository.delete(existRole);
//        }
    }
}
