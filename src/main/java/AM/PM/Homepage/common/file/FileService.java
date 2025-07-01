package AM.PM.Homepage.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    public String saveImage(MultipartFile file, String uploadPath) throws FileUploadException {
        validateFile(file);
        String originalName = file.getOriginalFilename();
        validateImageExtension(originalName);
        return saveFile(file, originalName, uploadPath);
    }

    public String saveFile(MultipartFile file, String uploadPath) throws FileUploadException {
        validateFile(file);
        String originalName = file.getOriginalFilename();
        return saveFile(file, originalName, uploadPath);
    }

    private String saveFile(MultipartFile file, String originalName, String uploadPath) throws FileUploadException {
        String extension = extractExtension(originalName);
        String uniqueFileName = UUID.randomUUID() + "." + extension;
        Path savePath = Paths.get(uploadPath, uniqueFileName);

        try {
            if (Files.notExists(savePath.getParent())) {
                Files.createDirectories(savePath.getParent());
            }

            Files.copy(file.getInputStream(), savePath);
            log.info("파일 저장 완료: {}", savePath);
            return savePath.toAbsolutePath().normalize().toString().replace("\\", "/");
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalName);
            throw new FileUploadException("파일 저장 중 오류 발생");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("파일 이름이 존재하지 않습니다.");
        }
    }

    private void validateImageExtension(String originalName) {
        String ext = extractExtension(originalName).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 확장자입니다.");
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일 확장자가 존재하지 않습니다.");
        }
        return fileName.substring(dotIndex + 1);
    }
}

