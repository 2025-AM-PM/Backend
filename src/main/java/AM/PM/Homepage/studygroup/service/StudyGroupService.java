package AM.PM.Homepage.studygroup.service;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.studygroup.entity.ApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import AM.PM.Homepage.studygroup.entity.StudyGroupRole;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import AM.PM.Homepage.studygroup.repository.StudyGroupApplicationRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupMemberRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupRepository;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.response.ApplicationApproveResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupApplyResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudyGroupService {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMemberRepository memberRepository;
    private final StudyGroupApplicationRepository applicationRepository;

    // 새로운 스터디 그룹 생성
    public StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest request, Long studentId) {
        log.info("[스터디 생성] 요청자 ID: {}, 제목: {}", studentId, request.getTitle());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Student Id"));

        StudyGroup studyGroup = StudyGroup.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .maxMember(request.getMaxMember())
                .status(StudyGroupStatus.RECRUITING)
                .build();

        StudyGroupMember leader = StudyGroupMember.builder()
                .student(student)
                .studyGroup(studyGroup)
                .role(StudyGroupRole.LEADER)
                .build();

        studyGroup.setLeader(leader);

        studyGroupRepository.save(studyGroup);
        memberRepository.save(leader);

        log.info("[스터디 생성 완료] ID: {}", studyGroup.getId());

        return StudyGroupCreateResponse.from(studyGroup);
    }

    // 특정 스터디 지원 신청
    public StudyGroupApplyResponse applyStudyGroup(Long studentId, Long groupId) {
        log.info("[스터디 신청] 사용자 ID: {}, 스터디 그룹 ID: {}", studentId, groupId);

        Student applicant = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Student Id"));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        if(studyGroup.getStatus() != StudyGroupStatus.RECRUITING) {
            log.warn("[스터디 모집 마감] 스터디 그룹 ID: {}", groupId);
            throw new IllegalArgumentException("모집 마감된 스터디");
        }

        if(memberRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[이미 가입된 스터디] 사용자 ID: {}, 스터디 그룹 ID: {}", studentId, groupId);
            throw new IllegalArgumentException("이미 가입된 스터디 그룹");
        }

        if(memberRepository.countByStudyGroup(studyGroup) >= studyGroup.getMaxMember()) {
            log.warn("[스터디 인원 초과] 스터디 그룹 ID: {}", groupId);
            throw new IllegalArgumentException("스터디 최대 인원 초과");
        }

        if (applicationRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[스터디 신청 중복] 사용자 ID: {}, 스터디 그룹 ID: {}", studentId, groupId);
            throw new IllegalArgumentException("이미 지원한 스터디 그룹");
        }

        StudyGroupApplication application = StudyGroupApplication.builder()
                .student(applicant)
                .studyGroup(studyGroup)
                .status(ApplicationStatus.PENDING)
                .build();

        applicationRepository.save(application);

        log.info("[스터디 신청 완료] 신청 ID: {}", application.getId());

        return StudyGroupApplyResponse.from(application);
    }

    // 스터디 지원 승인
    public ApplicationApproveResponse approveApplication(Long groupId, Long applicationId, Long leaderId) {
        log.info("[스터디 승인] 리더 ID: {}, 그룹 ID: {}, 신청 ID: {}", leaderId, groupId, applicationId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroup(applicationId, studyGroup)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Application Id"));

        Student leader = studentRepository.findById(leaderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Student Id"));

        if (!studyGroup.isLeader(leader)) {
            log.warn("[승인 실패] 리더 아님: {}", leaderId);
            throw new IllegalArgumentException("StudyGroup 리더만 승인 가능");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            log.warn("[승인 실패] 이미 처리된 신청 ID: {}, 상태: {}", applicationId, application.getStatus());
            throw new IllegalArgumentException("이미 처리된 신청");
        }

        application.approve();

        Student applicant = application.getStudent();
        StudyGroupMember studyGroupMember = StudyGroupMember.builder()
                .studyGroup(studyGroup)
                .student(applicant)
                .role(StudyGroupRole.MEMBER)
                .build();

        memberRepository.save(studyGroupMember);

        log.info("[승인 완료] 신청 ID: {}, 멤버 추가 완료 (studentId={})", applicationId, applicant.getId());

        return ApplicationApproveResponse.from(application);
    }
}
