package AM.PM.Homepage.exhibit.service;

import AM.PM.Homepage.common.file.FileService;
import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.entity.ExhibitImage;
import AM.PM.Homepage.exhibit.repository.ExhibitImageRepository;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
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

    @Value("${upload.path.exhibit.image}")
    private String imageUploadPath;

    @Transactional(readOnly = true)
    public Page<ExhibitSummaryResponse> findAllExhibit(Pageable pageable) {
        Page<Exhibit> exhibits = exhibitRepository.findAll(pageable);
        return exhibits.map(ExhibitSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ExhibitResponse findExhibitById(Long id) {
        Exhibit exhibit = exhibitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시"));

        return ExhibitResponse.from(exhibit);
    }

    public ExhibitSummaryResponse createExhibit(
            ExhibitCreateRequest request,
            List<MultipartFile> imageFiles,
            Long studentId
    ) throws FileUploadException {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생 id"));

        Exhibit exhibit = Exhibit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .exhibitUrl(request.getExhibitUrl())
                .githubUrl(request.getGithubUrl())
                .student(student)
                .build();

        if (imageFiles != null) {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile.isEmpty()) {
                    continue;
                }

                String originalImageName = imageFile.getOriginalFilename();
                String uploadImagePath = fileService.saveImage(imageFile, imageUploadPath);

                String uploadImageName = Paths.get(uploadImagePath).getFileName().toString();

                ExhibitImage image = ExhibitImage.builder()
                        .originalImageName(originalImageName)
                        .uploadImageName(uploadImageName)
                        .uploadImagePath(uploadImagePath)
                        .exhibit(exhibit)
                        .build();

                ExhibitImage savedImage = exhibitImageRepository.save(image);
                log.info("Exhibit 이미지 저장 성공: id={}, path={}", savedImage.getId(), savedImage.getUploadImagePath());

                exhibit.addImage(savedImage);
            }
        }

        Exhibit savedExhibit = exhibitRepository.save(exhibit);
        log.info("Exhibit 저장 완료: id={}, title={}", savedExhibit.getId(), savedExhibit.getTitle());

        return ExhibitSummaryResponse.from(savedExhibit);
    }
}
