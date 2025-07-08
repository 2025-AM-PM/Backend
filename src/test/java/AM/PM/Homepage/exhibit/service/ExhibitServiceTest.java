package AM.PM.Homepage.exhibit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import AM.PM.Homepage.common.file.FileService;
import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.repository.ExhibitImageRepository;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ExhibitServiceTest {

    @Mock
    FileService fileService;

    @Mock
    ExhibitRepository exhibitRepository;

    @Mock
    ExhibitImageRepository exhibitImageRepository;

    @Mock
    StudentRepository studentRepository;

    @InjectMocks
    ExhibitService exhibitService;

    @Test
    void saveWithoutImages() throws FileUploadException {
        ExhibitCreateRequest request = new ExhibitCreateRequest();
        request.setTitle("title");
        request.setDescription("description");
        request.setExhibitUrl("test url");

        Student student = new Student();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(exhibitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExhibitSummaryResponse response = exhibitService.createExhibit(request, new ArrayList<>(), 1L);

        assertThat(response.getTitle()).isEqualTo("title");
    }

    @Test
    void saveWithImages() throws FileUploadException {
        ExhibitCreateRequest request = new ExhibitCreateRequest();
        request.setTitle("title");
        request.setDescription("description");
        request.setExhibitUrl("test url");

        MultipartFile mockFile1 = mock(MultipartFile.class);
        when(mockFile1.isEmpty()).thenReturn(false);
        when(mockFile1.getOriginalFilename()).thenReturn("image1.jpg");
        MultipartFile mockFile2 = mock(MultipartFile.class);
        when(mockFile2.isEmpty()).thenReturn(false);
        when(mockFile2.getOriginalFilename()).thenReturn("image2.jpg");

        List<MultipartFile> files = List.of(mockFile1, mockFile2);

        Student student = new Student();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(fileService.storeFileToPath(any(), any())).thenReturn("/uploads/image.jpg");
        when(exhibitImageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(exhibitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExhibitSummaryResponse response = exhibitService.createExhibit(request, files, 1L);

        assertThat(response.getTitle()).isEqualTo("title");
    }

    @Test
    void findAll() {
        // given
        Student student = new Student();
        Exhibit exhibit1 = new Exhibit("title1", "des", "url", student);
        List<Exhibit> exhibits = List.of(exhibit1);
        Page<Exhibit> page = new PageImpl<>(exhibits);

        when(exhibitRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Page<ExhibitSummaryResponse> responses = exhibitService.findAllExhibit(PageRequest.of(0, 10));

        // then
        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getContent().getFirst().getTitle()).isEqualTo("title1");
    }

    @Test
    void find() {
        // given
        Student student = new Student();
        Exhibit exhibit1 = new Exhibit("title1", "des", "url", student);

        when(exhibitRepository.findById(any())).thenReturn(Optional.of(exhibit1));

        // when
        ExhibitResponse response = exhibitService.findExhibitById(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("title1");
    }
}