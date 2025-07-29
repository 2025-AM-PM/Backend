package AM.PM.Homepage.studygroup.service;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.studygroup.entity.*;
import AM.PM.Homepage.studygroup.repository.StudyGroupApplicationRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupMemberRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupRepository;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.request.StudyGroupUpdateRequest;
import AM.PM.Homepage.studygroup.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudyGroupService {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMemberRepository studyGroupMemberRepository;
    private final StudyGroupApplicationRepository applicationRepository;

    // 스터디 그룹 조회
    @Transactional(readOnly = true)
    public Page<StudyGroupSearchResponse> getStudyGroups(String title, StudyGroupStatus status, Pageable pageable) {
        if (title == null || title.isBlank()) {
            Page<StudyGroup> studyGroups = studyGroupRepository.findAllByStatus(status, pageable);
            return studyGroups.map(StudyGroupSearchResponse::from);
        }
        Page<StudyGroup> studyGroups = studyGroupRepository.findAllByTitleContainsIgnoreCaseAndStatus(title, status, pageable);
        return studyGroups.map(StudyGroupSearchResponse::from);
    }

    // 내 스터디 목록 조회
    @Transactional(readOnly = true)
    public List<MyStudyGroupResponse> getMyStudyGroups(Long userId) {
        List<StudyGroup> studyGroups = studyGroupRepository.findAllByUserId(userId);
        return studyGroups.stream()
                .map(MyStudyGroupResponse::from)
                .toList();
    }

    // 스터디 그룹 상세 조회
    @Transactional(readOnly = true)
    public StudyGroupDetailResponse getStudyGroupDetail(Long groupId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        List<StudyGroupMember> members = studyGroupMemberRepository.findAllByStudyGroup(studyGroup);

        return StudyGroupDetailResponse.from(studyGroup, members);
    }

    // 스터디 지원자 목록 조회
    @Transactional(readOnly = true)
    public List<StudyGroupApplicantResponse> getApplicants(Long groupId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[조회 실패] 리더 아님: {}", userId);
            throw new IllegalArgumentException("StudyGroup 리더만 조회 가능");
        }

        List<StudyGroupApplication> applications = applicationRepository.findAllByStudyGroupId(groupId);
        return applications.stream()
                .map(StudyGroupApplicantResponse::from)
                .toList();
    }

    // 내가 지원한 스터디 조회
    @Transactional(readOnly = true)
    public List<MyAppliedStudyGroupResponse> getMyAppliedStudyGroups(Long userId) {
        List<StudyGroupApplication> applications = applicationRepository.findAllByStudentId(userId);
        return applications.stream()
                .map(MyAppliedStudyGroupResponse::from)
                .toList();
    }

    // 새로운 스터디 그룹 생성
    public StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest request, Long userId) {
        log.info("[스터디 생성] 요청자 ID: {}, 제목: {}", userId, request.getTitle());

        Student user = studentRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User Id"));

        StudyGroup studyGroup = StudyGroup.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .maxMember(request.getMaxMember())
                .status(StudyGroupStatus.RECRUITING)
                .build();

        StudyGroupMember leader = StudyGroupMember.builder()
                .student(user)
                .studyGroup(studyGroup)
                .role(StudyGroupRole.LEADER)
                .build();

        studyGroup.setLeader(leader);

        studyGroupRepository.save(studyGroup);
        studyGroupMemberRepository.save(leader);

        log.info("[스터디 생성 완료] ID: {}", studyGroup.getId());

        return StudyGroupCreateResponse.from(studyGroup);
    }

    // 특정 스터디 지원 신청
    public StudyGroupApplyResponse applyStudyGroup(Long userId, Long groupId) {
        log.info("[스터디 신청] 사용자 ID: {}, 스터디 그룹 ID: {}", userId, groupId);

        Student applicant = studentRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User Id"));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        if (studyGroup.getStatus() != StudyGroupStatus.RECRUITING) {
            log.warn("[스터디 모집 마감] 스터디 그룹 ID: {}", groupId);
            throw new IllegalArgumentException("모집 마감된 스터디");
        }

        if (studyGroupMemberRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[이미 가입된 스터디] 사용자 ID: {}, 스터디 그룹 ID: {}", userId, groupId);
            throw new IllegalArgumentException("이미 가입된 스터디 그룹");
        }

        if (studyGroupMemberRepository.countByStudyGroup(studyGroup) >= studyGroup.getMaxMember()) {
            log.warn("[스터디 인원 초과] 스터디 그룹 ID: {}", groupId);
            throw new IllegalArgumentException("스터디 최대 인원 초과");
        }

        if (applicationRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[스터디 신청 중복] 사용자 ID: {}, 스터디 그룹 ID: {}", userId, groupId);
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
    public ApplicationApproveResponse approveApplication(Long groupId, Long applicationId, Long userId) {
        log.info("[스터디 승인] 유저 ID: {}, 그룹 ID: {}, 신청 ID: {}", userId, groupId, applicationId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보가 그룹에 속하지 않거나 존재하지 않음"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[승인 실패] 리더 아님: {}", userId);
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

        studyGroupMemberRepository.save(studyGroupMember);

        log.info("[승인 완료] 신청 ID: {}, 멤버 추가 완료 (studentId={})", applicationId, applicant.getId());

        return ApplicationApproveResponse.from(application);
    }

    // 스터디 지원 거절
    public void rejectApplication(Long groupId, Long applicationId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보가 그룹에 속하지 않거나 존재하지 않음"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[거절 실패] 리더 아님: {}", userId);
            throw new IllegalArgumentException("StudyGroup 리더만 거절 가능");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            log.warn("[거절 실패] 이미 처리된 신청 ID: {}, 상태: {}", applicationId, application.getStatus());
            throw new IllegalArgumentException("이미 처리된 신청");
        }

        application.reject();
        log.info("[거절 완료] 신청 ID: {} (studentId={})", applicationId, userId);
    }

    // 스터디 그룹 수정
    public StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest request, Long groupId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Group Id"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[수정 실패] 리더 아님: {}", userId);
            throw new IllegalArgumentException("StudyGroup 리더만 수정 가능");
        }

        studyGroup.update(
                request.getTitle(),
                request.getDescription(),
                request.getMaxMember(),
                request.getStatus()
        );

        return StudyGroupUpdateResponse.from(studyGroup);
    }

    public StudyGroupUpdateResponse updateStudyGroupStatus(StudyGroupStatusUpdateRequest request, Long groupId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Group Id"));

        Student user = studentRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User Id"));

        if (studyGroup.isNotLeader(user)) {
            log.warn("[상태 수정 실패] 리더 아님: {}", userId);
            throw new IllegalArgumentException("StudyGroup 리더만 상태 수정 가능");
        }

        studyGroup.setStatus(request.getStatus());
        log.info("[모집 상태 변경] groupId={}, userId={}, 상태={}", groupId, userId, request.getStatus());

        return StudyGroupUpdateResponse.from(studyGroup);
    }

    // 스터디 그룹 삭제
    public void deleteStudyGroup(Long groupId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Group Id"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[삭제 실패] 리더 아님: {}", userId);
            throw new IllegalArgumentException("StudyGroup 리더만 삭제 가능");
        }

        studyGroupRepository.delete(studyGroup);
        log.info("[스터디 삭제 완료] groupId={}, userId={}", groupId, userId);
    }

    // 스터디 지원 취소
    public void deleteStudyGroupApplication(Long groupId, Long applicationId, Long userId) {
        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보가 그룹에 속하지 않거나 존재하지 않음"));

        if (!application.getStudent().getId().equals(userId)) {
            log.warn("[취소 실패] 본인 아님: {}", userId);
            throw new IllegalArgumentException("본인만 지원 취소 가능");
        }

        applicationRepository.delete(application);
        log.info("[스터디 신청 취소] applicationId={}, groupId={}, userId={}", applicationId, groupId, userId);
    }

    // 스터디 탈퇴
    public void leaveStudyGroup(Long groupId, Long userId) {
        StudyGroupMember member = studyGroupMemberRepository.findByStudyGroupIdAndStudentId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버"));

        if (member.getRole() == StudyGroupRole.LEADER) {
            log.warn("[스터디 탈퇴 실패] 리더는 탈퇴 불가: groupId={}, userId={}", groupId, userId);
            throw new IllegalArgumentException("스터디 리더는 탈퇴할 수 없습니다. 삭제를 사용하세요.");
        }

        studyGroupMemberRepository.delete(member);
        log.info("[스터디 탈퇴] groupId={}, userId={}", groupId, userId);
    }

    // 스터디 멤버 강제 탈퇴
    public void removeMember(Long groupId, Long groupMemberId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 StudyGroup Id"));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            throw new IllegalArgumentException("스터디 리더만 멤버를 삭제할 수 있습니다.");
        }

        StudyGroupMember member = studyGroupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버"));

        if (!member.getStudyGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("소속되지 않은 멤버입니다.");
        }

        if (member.getId().equals(studyGroup.getLeader().getId())) {
            throw new IllegalArgumentException("리더는 삭제할 수 없습니다.");
        }

        studyGroupMemberRepository.delete(member);
    }
}
