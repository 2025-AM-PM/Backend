package AM.PM.Homepage.exhibit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import AM.PM.Homepage.common.file.FileService;
import AM.PM.Homepage.exhibit.repository.ExhibitImageRepository;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.request.ExhibitUpdateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.util.constant.StudentRole;
import java.util.ArrayList;
import java.util.List;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@SpringBootTest
class ExhibitServiceBootTest {

    @MockitoBean
    FileService fileService;

    @Autowired
    ExhibitRepository exhibitRepository;

    @Autowired
    ExhibitImageRepository exhibitImageRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ExhibitService exhibitService;

    ExhibitCreateRequest createRequest;
    ExhibitUpdateRequest updateRequest;

    List<MultipartFile> files;
    List<MultipartFile> emptyFile = new ArrayList<>();
    Student student = Student.builder()
            .studentNumber("123456")
            .studentName("김학생")
            .studentRole(StudentRole.ROLE_STUDENT.name())
            .build();
    Long studentId;

    @BeforeEach
    void setUp() throws FileUploadException {
        when(fileService.storeFileToPath(any(), any())).thenReturn("/mock/path/image.jpg");

        createRequest = new ExhibitCreateRequest();
        createRequest.setTitle("title");
        createRequest.setDescription("des");
        createRequest.setExhibitUrl("url");

        updateRequest = new ExhibitUpdateRequest();
        updateRequest.setTitle("update title");
        updateRequest.setDescription("update des");
        updateRequest.setExhibitUrl("update url");

        MockMultipartFile mockImage1 = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "fake-image-content-1".getBytes()
        );

        MockMultipartFile mockImage2 = new MockMultipartFile(
                "files",
                "image2.jpg",
                "image/jpeg",
                "fake-image-content-2".getBytes()
        );

        files = new ArrayList<>();
        files.add(mockImage1);
        files.add(mockImage2);

        Student save = studentRepository.save(student);
        studentId = save.getId();
    }

    @AfterEach
    void tearDown() {
        exhibitImageRepository.deleteAll();
        exhibitRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void saveWithoutImages() throws FileUploadException {
        // given

        // when
        ExhibitSummaryResponse response = exhibitService.createExhibit(createRequest, emptyFile, studentId);

        // then
        assertThat(response.getTitle()).isEqualTo("title");
    }

    @Test
    void saveWithImages() throws FileUploadException {
        // given

        // when
        ExhibitSummaryResponse response = exhibitService.createExhibit(createRequest, files, studentId);

        // then
        assertThat(response.getTitle()).isEqualTo("title");
        assertThat(response.getThumbnailUrl()).isNotNull();
    }

    @Test
    void findAll() throws FileUploadException {
        // given
        exhibitService.createExhibit(createRequest, files, studentId);

        // when
        Page<ExhibitSummaryResponse> responses = exhibitService.findAllExhibit(PageRequest.of(0, 10));

        // then
        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getContent().getFirst().getThumbnailUrl()).isNotNull();
    }

    @Test
    void find() throws FileUploadException {
        // given
        ExhibitSummaryResponse res = exhibitService.createExhibit(createRequest, files, studentId);
        Long id = res.getId();

        // when
        ExhibitResponse response = exhibitService.findExhibitById(id);

        // then
        assertThat(response.getImageUrls().size()).isEqualTo(2);
    }

    @Test
    void update() throws FileUploadException {
        // given
        ExhibitSummaryResponse res = exhibitService.createExhibit(createRequest, files, studentId);
        Long id = res.getId();

        // when
        ExhibitSummaryResponse response = exhibitService.updateExhibit(id, updateRequest, files, new UserAuth(student));

        // then
        assertThat(response.getTitle()).isEqualTo("update title");
    }

    @Test
    void delete() throws FileUploadException {
        // given
        ExhibitSummaryResponse res = exhibitService.createExhibit(createRequest, files, studentId);
        Long id = res.getId();

        // when
        exhibitService.deleteExhibit(id, new UserAuth(student));

        // then
        Page<ExhibitSummaryResponse> responses = exhibitService.findAllExhibit(PageRequest.of(0, 10));
        assertThat(responses.getTotalElements()).isEqualTo(0);
    }

    @Test
    void update_다른_사용자_예외_발생() throws FileUploadException {
        ExhibitSummaryResponse saved = exhibitService.createExhibit(createRequest, files, studentId);
        Long exhibitId = saved.getId();

        Student another = Student.builder()
                .studentName("다른 사람")
                .studentNumber("999999")
                .studentRole(StudentRole.ROLE_STUDENT.name())
                .build();
        studentRepository.save(another);

        UserAuth otherAuth = new UserAuth(another);

        assertThatThrownBy(() -> exhibitService.updateExhibit(exhibitId, updateRequest, files, otherAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("권한 없음");
    }
}