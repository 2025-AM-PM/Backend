package AM.PM.Homepage.exhibit.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.common.file.FileService;
import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.entity.ExhibitImage;
import AM.PM.Homepage.exhibit.repository.ExhibitImageRepository;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.request.ExhibitUpdateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitService {

    private final FileService fileService;
    private final ExhibitRepository exhibitRepository;
    private final ExhibitImageRepository exhibitImageRepository;
    private final StudentRepository studentRepository;

    @Value("${storage.base.path}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Page<ExhibitSummaryResponse> findAllExhibit(Pageable pageable) {
        return exhibitRepository.findAll(pageable).map(ExhibitSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ExhibitResponse findExhibitById(Long id) {
        Exhibit exhibit = findOrThrowExhibitById(id);
        return ExhibitResponse.from(exhibit);
    }

    public ExhibitSummaryResponse createExhibit(
            ExhibitCreateRequest request,
            List<MultipartFile> imageFiles,
            Long studentId
    ) throws FileUploadException {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        Exhibit exhibit = Exhibit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .exhibitUrl(request.getExhibitUrl())
                .student(student)
                .build();

        Exhibit savedExhibit = exhibitRepository.save(exhibit);

        addExhibitImageFiles(imageFiles, exhibit);
        log.debug("Exhibit 저장 완료: id={}, title={}", savedExhibit.getId(), savedExhibit.getTitle());

        return ExhibitSummaryResponse.from(savedExhibit);
    }

    public ExhibitSummaryResponse updateExhibit(
            Long exhibitId,
            ExhibitUpdateRequest request,
            List<MultipartFile> imageFiles,
            UserAuth user
    ) throws FileUploadException {

        Exhibit exhibit = findOrThrowExhibitById(exhibitId);

        validateExhibitOwnership(exhibit, user);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            deleteExhibitImages(exhibit);
            addExhibitImageFiles(imageFiles, exhibit);
        }

        exhibit.update(
                request.getTitle(),
                request.getDescription(),
                request.getExhibitUrl()
        );

        log.debug("Exhibit 수정 성공: id={}, title={}", exhibit.getId(), exhibit.getTitle());
        return ExhibitSummaryResponse.from(exhibit);
    }

    public void deleteExhibit(Long exhibitId, UserAuth user) {
        Exhibit exhibit = findOrThrowExhibitById(exhibitId);

        validateExhibitOwnership(exhibit, user);

        deleteExhibitImages(exhibit);
        exhibitRepository.delete(exhibit);

        log.debug("Exhibit 삭제 성공: title={}", exhibit.getTitle());
    }

    private void addExhibitImageFiles(List<MultipartFile> imageFiles, Exhibit exhibit) throws FileUploadException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) {
                continue;
            }

            String originalImageName = imageFile.getOriginalFilename();
            String uploadImagePath = fileService.storeFileToPath(imageFile, uploadDir);
            String uploadImageName = Paths.get(uploadImagePath).getFileName().toString();

            ExhibitImage image = ExhibitImage.builder()
                    .originalImageName(originalImageName)
                    .uploadImageName(uploadImageName)
                    .uploadImagePath(uploadImagePath)
                    .exhibit(exhibit)
                    .build();

            exhibitImageRepository.save(image);
            exhibit.addImage(image);
            log.debug("Exhibit 이미지 저장: {}", uploadImagePath);
        }
    }

    private void deleteExhibitImages(Exhibit exhibit) {
        List<ExhibitImage> images = exhibit.getImages();
        images.forEach(img -> fileService.deleteFile(img.getUploadImagePath()));
        exhibitImageRepository.deleteAll(images);
        exhibit.clearImages();
    }

    private Exhibit findOrThrowExhibitById(Long id) {
        return exhibitRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EXHIBIT));
    }

    private void validateExhibitOwnership(Exhibit exhibit, UserAuth user) {
        if (!exhibit.getStudentId().equals(user.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_EXHIBIT);
        }
    }
}
