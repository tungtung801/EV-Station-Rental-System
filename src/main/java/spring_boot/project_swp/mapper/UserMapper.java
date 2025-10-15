package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring_boot.project_swp.dto.request.user_request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.user_response.UserLoginResponse;
import spring_boot.project_swp.dto.response.user_response.UserResponse;
import spring_boot.project_swp.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //for Login
    @Mapping(source = "role.roleName", target = "roleName")
    UserLoginResponse toUserLoginResponse(User user);

    //for register
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserRegistrationRequest userRegistrationRequest);

    //entity to dto
    @Mapping(source = "role.roleName", target = "roleName")
    UserResponse toUserResponse(User user);
}
