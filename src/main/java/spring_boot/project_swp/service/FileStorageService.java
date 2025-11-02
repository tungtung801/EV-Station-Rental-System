package spring_boot.project_swp.service;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  String saveFile(MultipartFile file);

  Path loadFile(String filename);

  void deleteFile(String filename);
}
