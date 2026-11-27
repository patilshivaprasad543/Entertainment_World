package in.sp.main.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Safely stores uploaded company logos and legal verification documents on disk.
 */
@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize upload folder", ex);
        }
    }

    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isBlank()) {
            safeName = "document";
        }
        String storedName = UUID.randomUUID().toString().substring(0, 8) + "_" + safeName;
        Path dir = this.rootLocation.resolve(subFolder).normalize();
        if (!dir.startsWith(this.rootLocation)) {
            throw new IllegalArgumentException("Invalid upload path");
        }
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subFolder + "/" + storedName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file " + safeName, ex);
        }
    }
}
