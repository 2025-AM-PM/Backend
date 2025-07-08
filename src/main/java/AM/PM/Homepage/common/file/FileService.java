package AM.PM.Homepage.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
public class FileService {

    public String storeFileToPath(MultipartFile file, String uploadPath) throws FileUploadException {
        validateUploadPath(uploadPath);
        validateFile(file);
        String originalName = file.getOriginalFilename();
        return storeFileToPath(file, originalName, uploadPath);
    }

    public void deleteFile(String path) {
        try {
            if (Files.deleteIfExists(Path.of(path))) {
                log.info("파일 삭제 성공: {}", path);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String storeFileToPath(MultipartFile file, String originalName, String uploadPath)
            throws FileUploadException {
        String uniqueFileName = generateUniqueFilename(originalName);
        Path savePath = Paths.get(uploadPath, uniqueFileName);

        try {
            if (Files.notExists(savePath.getParent())) {
                Files.createDirectories(savePath.getParent());
            }

            Files.copy(file.getInputStream(), savePath);
            log.info("파일 저장 완료: {}", savePath);
            return savePath.toAbsolutePath().normalize().toString().replace("\\", "/");
        } catch (IOException e) {
            log.error("파일 저장 실패: {} - {}", originalName, e.getMessage());
            throw new FileUploadException("파일 저장 중 오류 발생");
        }
    }

    private void validateUploadPath(String uploadPath) {
        if (uploadPath == null || uploadPath.isBlank()) {
            throw new IllegalArgumentException("업로드 경로가 잘못되었습니다.");
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

    private static String generateUniqueFilename(String originalName) {
        String sanitized = originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return UUID.randomUUID() + "_" + sanitized;
    }
}

