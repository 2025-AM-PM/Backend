package AM.PM.Homepage.exhibit.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.request.ExhibitUpdateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Page<ExhibitSummaryResponse> findAllExhibit(Pageable pageable) {
        return exhibitRepository.findAll(pageable).map(ExhibitSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ExhibitResponse findExhibitById(Long id) {
        Exhibit exhibit = findOrThrowExhibitById(id);
        return ExhibitResponse.from(exhibit);
    }

    public ExhibitSummaryResponse createExhibit(ExhibitCreateRequest request, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN_NOT_OWNER));

        Exhibit exhibit = Exhibit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .exhibitUrl(request.getExhibitUrl())
                .student(student)
                .build();
        exhibitRepository.save(exhibit);

        return ExhibitSummaryResponse.from(exhibit);
    }

    public ExhibitSummaryResponse updateExhibit(
            Long exhibitId,
            ExhibitUpdateRequest request
    ) {
        Exhibit exhibit = findOrThrowExhibitById(exhibitId);

        exhibit.update(
                request.getTitle(),
                request.getDescription(),
                request.getExhibitUrl()
        );

        log.debug("Exhibit 수정 성공: id={}, title={}", exhibit.getId(), exhibit.getTitle());
        return ExhibitSummaryResponse.from(exhibit);
    }

    public void deleteExhibit(Long exhibitId) {
        Exhibit exhibit = findOrThrowExhibitById(exhibitId);

        exhibitRepository.delete(exhibit);
        log.debug("Exhibit 삭제 성공: title={}", exhibit.getTitle());
    }

    private Exhibit findOrThrowExhibitById(Long exhibitId) {
        return exhibitRepository.findById(exhibitId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EXHIBIT));
    }
}
