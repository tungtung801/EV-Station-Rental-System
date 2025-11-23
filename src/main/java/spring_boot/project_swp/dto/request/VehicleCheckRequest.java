package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VehicleCheckRequest {

  @NotNull(message = "Rental ID cannot be null")
  Long rentalId;

  String notes;

  // MultipartFile list cho upload ảnh
  List<MultipartFile> images;
}
