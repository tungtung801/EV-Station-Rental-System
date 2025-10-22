package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IncidentReportRequest {

    @NotNull(message = "Rental ID không được để trống")
    Long rentalId;

    @NotNull(message = "Vehicle ID không được để trống")
    Long vehicleId;

    @NotNull(message = "User ID không được để trống")
    Long userId;

    Long checkId; // Nullable as discussed

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @NotBlank(message = "Trạng thái không được để trống")
    String status;

    String imageUrls;
}