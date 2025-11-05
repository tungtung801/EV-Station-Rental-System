package spring_boot.project_swp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentUploadRequest {

  @NotNull(message = "ID card file cannot be null")
  MultipartFile idCardFile;

  @NotNull(message = "Driving license file cannot be null")
  MultipartFile drivingLicenseFile;
}
