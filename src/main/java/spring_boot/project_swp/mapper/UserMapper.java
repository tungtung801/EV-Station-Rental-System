package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.request.UserUpdateRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.User;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  // for Login
  @Mapping(source = "role.roleName", target = "roleName")
  UserLoginResponse toUserLoginResponse(User user);

  // for register
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "accountStatus", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  User toUser(UserRegistrationRequest userRegistrationRequest);

  // entity to dto
  @Mapping(source = "role.roleName", target = "roleName")
  UserResponse toUserResponse(User user);

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "accountStatus", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  void updateUser(@MappingTarget User user, UserUpdateRequest request);

  // Add method to convert UserResponse to User entity (fix PaymentServiceImpl error)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "accountStatus", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  User toEntity(UserResponse userResponse);
}
