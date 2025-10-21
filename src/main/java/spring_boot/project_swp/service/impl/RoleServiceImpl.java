package spring_boot.project_swp.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.dto.response.RoleResponse;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RoleMapper;
import spring_boot.project_swp.repository.RoleRepository;
import spring_boot.project_swp.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleServiceImpl implements RoleService {
    final RoleRepository roleRepository;
    final RoleMapper roleMapper;

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> roleResponses = new ArrayList<>();
        for (Role role : roles) {
            roleResponses.add(roleMapper.toRoleResponse(role));
        }
        return roleResponses;
    }

    @Override
    public Optional<RoleResponse> findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName).map(roleMapper::toRoleResponse);
    }

    @Override
    public Optional<RoleResponse> findByRoleId(int roleId) {
        return roleRepository.findById(roleId).map(roleMapper::toRoleResponse);
    }

    @Override
    public Role getRoleByName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new NotFoundException("Role not found"));
        return role;
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
            throw new ConflictException("Role already exists");
        }
        Role newRole = roleMapper.toRole(request);
        Role savedRole = roleRepository.save(newRole);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(int roleId, RoleRequest request) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));

        if (roleRepository.findByRoleName(request.getRoleName()).isPresent() && !existingRole.getRoleName().equals(request.getRoleName())) {
            throw new ConflictException("Role name already exists");
        }

        existingRole.setRoleName(request.getRoleName());
        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public void deleteRole(int roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new NotFoundException("Role not found with id: " + roleId);
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public RoleResponse getRoleById(int roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
        return roleMapper.toRoleResponse(role);
    }
}
