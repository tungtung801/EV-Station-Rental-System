package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.respone.UserLoginResponse;
import spring_boot.project_swp.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    UserLoginResponse requestToUserLoginResponse(UserLoginRequest request);

    // Hoặc tốt hơn: map từ User entity
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phoneNumber", target = "phoneNumber")
    @Mapping(source = "user.role.roleName", target = "roleName")
    UserLoginResponse userToUserLoginResponse(User user);
}
