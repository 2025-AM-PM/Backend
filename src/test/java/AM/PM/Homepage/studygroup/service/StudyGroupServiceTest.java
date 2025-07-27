package AM.PM.Homepage.studygroup.service;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.studygroup.entity.ApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.response.ApplicationApproveResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupApplyResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupCreateResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class StudyGroupServiceTest {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudyGroupService studyGroupService;

    Student student01;
    Student student02;

    @BeforeEach
    void setUp() {
        student01 = new Student(new StudentResponse("1111", "010-1111-1111", "student01"));
        student02 = new Student(new StudentResponse("2222", "010-2222-2222", "student02"));

        studentRepository.save(student01);
        studentRepository.save(student02);
    }

    @Test
    void 스터디그룹_전체_테스트() {
        // 생성
        StudyGroupCreateResponse createResponse = studyGroupService.createStudyGroup(
                new StudyGroupCreateRequest("01", "study01", 10), 1L);

        // 가입 신청
        StudyGroupApplyResponse applyResponse = studyGroupService.applyStudyGroup(2L, 1L);

        // 가입 신청 승인
        ApplicationApproveResponse approveResponse = studyGroupService.approveApplication(1L, 1L, 1L);
        assertThat(approveResponse.getStatus()).isEqualTo(ApplicationStatus.APPROVED);

        // 스터디 조회
        Page<StudyGroupSearchResponse> searchResponse = studyGroupService.getStudyGroups("01", StudyGroupStatus.RECRUITING, PageRequest.of(0, 10));
        System.out.println(searchResponse);
        StudyGroupSearchResponse g1 = searchResponse.getContent().getFirst();
        assertThat(g1.getLeader()).isEqualTo("student01");
    }
}