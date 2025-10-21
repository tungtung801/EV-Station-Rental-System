package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.dto.response.RoleResponse;
import spring_boot.project_swp.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    Role toRole(RoleRequest request);

    @Mapping(source = "roleId", target = "roleId")
    @Mapping(source = "roleName", target = "roleName")
    RoleResponse toRoleResponse(Role role);
}