package AM.PM.Homepage.studygroup.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import AM.PM.Homepage.studygroup.entity.StudyGroupRole;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import AM.PM.Homepage.studygroup.repository.StudyGroupApplicationRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupMemberRepository;
import AM.PM.Homepage.studygroup.repository.StudyGroupRepository;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.request.StudyGroupUpdateRequest;
import AM.PM.Homepage.studygroup.response.ApplicationApproveResponse;
import AM.PM.Homepage.studygroup.response.MyAppliedStudyGroupResponse;
import AM.PM.Homepage.studygroup.response.MyStudyGroupResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupApplicantResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupApplyResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupCreateResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupDetailResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupSearchResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupStatusUpdateRequest;
import AM.PM.Homepage.studygroup.response.StudyGroupUpdateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("[스터디 그룹 조회] title={}, status={}, page={}, size={}",
                title, status, pageable.getPageNumber(), pageable.getPageSize());

        if (title == null || title.isBlank()) {
            Page<StudyGroupSearchResponse> result = studyGroupRepository.findAllByStatus(status, pageable)
                    .map(StudyGroupSearchResponse::from);
            log.info("[스터디 그룹 조회 완료] 조회된 그룹={}개, 전체={}개", result.getNumberOfElements(), result.getTotalElements());
            return result;
        }
        Page<StudyGroupSearchResponse> result = studyGroupRepository.findAllByTitleContainsIgnoreCaseAndStatus(title, status, pageable)
                .map(StudyGroupSearchResponse::from);
        log.info("[스터디 그룹 조회 완료] 조회된 그룹={}개, 전체={}개", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    // 내 스터디 목록 조회
    @Transactional(readOnly = true)
    public List<MyStudyGroupResponse> getMyStudyGroups(Long userId) {
        log.info("[내 스터디 목록 조회] userId={}", userId);

        List<MyStudyGroupResponse> result = studyGroupRepository.findAllByUserId(userId)
                .stream()
                .map(MyStudyGroupResponse::from)
                .toList();

        log.info("[내 스터디 목록 조회 완료] userId={}, 스터디 개수={}개", userId, result.size());
        return result;
    }

    // 스터디 그룹 상세 조회
    @Transactional(readOnly = true)
    public StudyGroupDetailResponse getStudyGroupDetail(Long groupId) {
        log.info("[스터디 그룹 상세 조회] groupId={}", groupId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        List<StudyGroupMember> members = studyGroupMemberRepository.findAllByStudyGroup(studyGroup);

        log.info("[스터디 그룹 상세 조회 완료] groupId={}, 멤버 수={}명", groupId, members.size());
        return StudyGroupDetailResponse.from(studyGroup, members);
    }

    // 스터디 지원자 목록 조회
    @Transactional(readOnly = true)
    public List<StudyGroupApplicantResponse> getApplicants(Long groupId, Long userId) {
        log.info("[스터디 지원자 목록 조회] groupId={}, userId={}", groupId, userId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        List<StudyGroupApplicantResponse> result = applicationRepository.findAllByStudyGroupId(groupId)
                .stream()
                .map(StudyGroupApplicantResponse::from)
                .toList();

        log.info("[스터디 지원자 목록 조회 완료] groupId={}, 지원자 수={}명", groupId, result.size());
        return result;
    }

    // 내가 지원한 스터디 조회
    @Transactional(readOnly = true)
    public List<MyAppliedStudyGroupResponse> getMyAppliedStudyGroups(Long userId) {
        log.info("[내가 지원한 스터디 조회] userId={}", userId);

        List<MyAppliedStudyGroupResponse> result = applicationRepository.findAllByStudentId(userId)
                .stream()
                .map(MyAppliedStudyGroupResponse::from)
                .toList();

        log.info("[내가 지원한 스터디 조회 완료] userId={}, 지원한 스터디={}개", userId, result.size());
        return result;
    }

    // 새로운 스터디 그룹 생성
    public StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest request, Long userId) {
        log.info("[스터디 생성] userId={}, 제목={}", userId, request.getTitle());

        Student user = studentRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

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

        log.info("[스터디 생성 완료] groupId={}, userId={}", studyGroup.getId(), userId);
        return StudyGroupCreateResponse.from(studyGroup);
    }

    // 특정 스터디 지원 신청
    public StudyGroupApplyResponse applyStudyGroup(Long userId, Long groupId) {
        log.info("[스터디 신청] userId={}, groupId={}", userId, groupId);

        Student applicant = studentRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        if (studyGroup.getStatus() != StudyGroupStatus.RECRUITING) {
            log.warn("[모집 마감] groupId={}", groupId);
            throw new CustomException(ErrorCode.CLOSED_STUDY_GROUP);
        }

        if (studyGroupMemberRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[이미 가입된 스터디] userId={}, groupId={}", userId, groupId);
            throw new CustomException(ErrorCode.ALREADY_JOINED_STUDY_GROUP);
        }

        if (studyGroupMemberRepository.countByStudyGroup(studyGroup) >= studyGroup.getMaxMember()) {
            log.warn("[스터디 인원 초과] groupId={}", groupId);
            throw new CustomException(ErrorCode.FULL_STUDY_GROUP);
        }

        if (applicationRepository.existsByStudentAndStudyGroup(applicant, studyGroup)) {
            log.warn("[스터디 신청 중복] userId={}, groupId={}", userId, groupId);
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        StudyGroupApplication application = StudyGroupApplication.builder()
                .student(applicant)
                .studyGroup(studyGroup)
                .status(StudyGroupApplicationStatus.PENDING)
                .build();

        applicationRepository.save(application);
        log.info("[스터디 신청 완료] applicationId={}, userId={}, groupId={}", application.getId(), userId, groupId);
        return StudyGroupApplyResponse.from(application);
    }

    // 스터디 지원 승인
    public ApplicationApproveResponse approveApplication(Long groupId, Long applicationId, Long userId) {
        log.info("[스터디 승인] userId={}, groupId={}, applicationId={}", userId, groupId, applicationId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLICATION));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[승인 실패] 리더 아님: userId={}", userId);
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        if (application.getStatus() != StudyGroupApplicationStatus.PENDING) {
            log.warn("[승인 실패] 이미 처리된 신청: applicationId={}, status={}", applicationId, application.getStatus());
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        application.approve();

        Student applicant = application.getStudent();
        StudyGroupMember member = StudyGroupMember.builder()
                .studyGroup(studyGroup)
                .student(applicant)
                .role(StudyGroupRole.MEMBER)
                .build();

        studyGroupMemberRepository.save(member);
        log.info("[승인 완료] applicationId={}, studentId={}, groupId={}", applicationId, applicant.getId(), groupId);
        return ApplicationApproveResponse.from(application);
    }

    // 스터디 지원 거절
    public void rejectApplication(Long groupId, Long applicationId, Long userId) {
        log.info("[스터디 거절] userId={}, groupId={}, applicationId={}", userId, groupId, applicationId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLICATION));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            log.warn("[거절 실패] 리더 아님: userId={}", userId);
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        if (application.getStatus() != StudyGroupApplicationStatus.PENDING) {
            log.warn("[거절 실패] 이미 처리된 신청: applicationId={}, status={}", applicationId, application.getStatus());
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        application.reject();
        log.info("[거절 완료] applicationId={}, userId={}, groupId={}", applicationId, userId, groupId);
    }

    // 스터디 그룹 수정
    public StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest request, Long groupId, Long userId) {
        log.info("[스터디 수정] groupId={}, userId={}", groupId, userId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        studyGroup.update(
                request.getTitle(),
                request.getDescription(),
                request.getMaxMember(),
                request.getStatus()
        );

        log.info("[스터디 수정 완료] groupId={}, userId={}", groupId, userId);
        return StudyGroupUpdateResponse.from(studyGroup);
    }

    public StudyGroupUpdateResponse updateStudyGroupStatus(StudyGroupStatusUpdateRequest request, Long groupId, Long userId) {
        log.info("[모집 상태 변경] groupId={}, userId={}, status={}", groupId, userId, request.getStatus());

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        Student user = studentRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        if (studyGroup.isNotLeader(user)) {
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        studyGroup.setStatus(request.getStatus());
        log.info("[모집 상태 변경 완료] groupId={}, userId={}, status={}", groupId, userId, request.getStatus());
        return StudyGroupUpdateResponse.from(studyGroup);
    }

    // 스터디 그룹 삭제
    public void deleteStudyGroup(Long groupId, Long userId) {
        log.info("[스터디 삭제] groupId={}, userId={}", groupId, userId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        studyGroupRepository.delete(studyGroup);
        log.info("[스터디 삭제 완료] groupId={}, userId={}", groupId, userId);
    }

    // 스터디 지원 취소
    public void deleteStudyGroupApplication(Long groupId, Long applicationId, Long userId) {
        log.info("[스터디 신청 취소] applicationId={}, groupId={}, userId={}", applicationId, groupId, userId);

        StudyGroupApplication application = applicationRepository.findByIdAndStudyGroupId(applicationId, groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLICATION));

        if (!application.getStudent().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_NOT_OWNER);
        }

        applicationRepository.delete(application);
        log.info("[스터디 신청 취소 완료] applicationId={}, groupId={}, userId={}", applicationId, groupId, userId);
    }

    // 스터디 탈퇴
    public void leaveStudyGroup(Long groupId, Long userId) {
        log.info("[스터디 탈퇴] groupId={}, userId={}", groupId, userId);

        StudyGroupMember member = studyGroupMemberRepository.findByStudyGroupIdAndStudentId(groupId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (member.getRole() == StudyGroupRole.LEADER) {
            throw new CustomException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        studyGroupMemberRepository.delete(member);
        log.info("[스터디 탈퇴 완료] groupId={}, userId={}", groupId, userId);
    }

    // 스터디 멤버 강제 탈퇴
    public void removeMember(Long groupId, Long groupMemberId, Long userId) {
        log.info("[멤버 강제 탈퇴] groupId={}, memberId={}, userId={}", groupId, groupMemberId, userId);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDY_GROUP));

        if (!studyGroup.getLeader().getStudent().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_GROUP_LEADER_ONLY);
        }

        StudyGroupMember member = studyGroupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (!member.getStudyGroup().getId().equals(groupId)) {
            throw new CustomException(ErrorCode.NOT_BELONG_TO_GROUP);
        }

        if (member.getId().equals(studyGroup.getLeader().getId())) {
            throw new CustomException(ErrorCode.LEADER_CANNOT_BE_REMOVED);
        }

        studyGroupMemberRepository.delete(member);
        log.info("[멤버 강제 탈퇴 완료] groupId={}, memberId={}, userId={}", groupId, groupMemberId, userId);
    }
}

