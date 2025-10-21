package spring_boot.project_swp.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.service.FileStorageService;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + fileExtension;
        try {
            Path copyLocation = Paths.get(uploadDir + "/" + filename);
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new ConflictException("Could not store file " + originalFilename + ". Please try again!");
        }
    }

    @Override
    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path fileToDelete = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            throw new ConflictException("Could not delete file " + filename + ". Please try again!");
        }
    }
}