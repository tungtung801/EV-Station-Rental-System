package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationAddingRequest {
    @NotBlank(message = "Tên trạm không được để trống")
    String stationName;

    @NotBlank(message = "Địa chỉ không được để trống")
    String address;

    @NotNull(message = "Vĩ độ không được để trống")
    BigDecimal latitude;

    @NotNull(message = "Kinh độ không được để trống")
    BigDecimal longitude;

    @Min(value = 1, message = "Tổng số chỗ phải lớn hơn 0")
    int totalDocks;

    @NotNull(message = "ID của địa điểm (phường/xã) không được để trống")
    Integer locationId;
}
