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

  // --- LOGIN ---
  @Mapping(source = "role.roleName", target = "roleName")
  UserLoginResponse toUserLoginResponse(User user);

  // --- REGISTER ---
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "accountStatus", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "password", ignore = true) // Ignore vì sẽ set sau khi mã hóa
  @Mapping(target = "role", ignore = true) // Ignore vì set tay trong Service
  User toUser(UserRegistrationRequest userRegistrationRequest);

  // --- GET USER (RESPONSE) ---
  @Mapping(source = "role.roleName", target = "roleName")
  @Mapping(source = "profile.status", target = "kycStatus") // <--- QUAN TRỌNG: Lấy status KYC
  @Mapping(
      source = "station.stationName",
      target = "stationName") // <--- QUAN TRỌNG: Lấy tên trạm (nếu là Staff)
  UserResponse toUserResponse(User user);

  // --- UPDATE USER ---
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "password", ignore = true) // Password update qua API riêng hoặc set tay
  @Mapping(target = "accountStatus", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  void updateUser(@MappingTarget User user, UserUpdateRequest request);

  // ĐÃ XÓA hàm toEntity() -> Tuyệt đối không khôi phục lại.
}
