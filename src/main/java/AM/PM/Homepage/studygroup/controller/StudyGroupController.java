package AM.PM.Homepage.studygroup.controller;

import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.request.StudyGroupUpdateRequest;
import AM.PM.Homepage.studygroup.response.*;
import AM.PM.Homepage.studygroup.service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    // 스터디 목록 조회
    @GetMapping
    public ResponseEntity<Page<StudyGroupSearchResponse>> getStudyGroup(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "RECRUITING") StudyGroupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction dir
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<StudyGroupSearchResponse> response = studyGroupService.getStudyGroups(title, status, pageable);
        return ResponseEntity.ok(response);
    }

    // 내 스터디 목록 조회
    @GetMapping("/me")
    public ResponseEntity<List<MyStudyGroupResponse>> getMyStudyGroups(
            @AuthenticationPrincipal UserAuth user
    ) {
        List<MyStudyGroupResponse> response = studyGroupService.getMyStudyGroups(user.getId());
        return ResponseEntity.ok(response);
    }

    // 스터디 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroupDetailResponse> getStudyGroupDetail(@PathVariable Long groupId) {
        StudyGroupDetailResponse response = studyGroupService.getStudyGroupDetail(groupId);
        return ResponseEntity.ok(response);
    }

    // 스터디 지원자 목록 조회 (리더만)
    @GetMapping("/{groupId}/applications")
    public ResponseEntity<List<StudyGroupApplicantResponse>> getApplicants(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth user
    ) {
        List<StudyGroupApplicantResponse> response = studyGroupService.getApplicants(groupId, user.getId());
        return ResponseEntity.ok(response);
    }

    // 본인이 지원한 스터디 목록 조회 (본인만)
    @GetMapping("/api/study-groups/applications/me")
    public ResponseEntity<List<MyAppliedStudyGroupResponse>> getMyAppliedStudyGroups(
            @AuthenticationPrincipal UserAuth user
    ) {
        List<MyAppliedStudyGroupResponse> response = studyGroupService.getMyAppliedStudyGroups(user.getId());
        return ResponseEntity.ok(response);
    }

    // 새로운 스터디 생성
    @PostMapping
    public ResponseEntity<StudyGroupCreateResponse> createStudyGroup(
            @RequestBody StudyGroupCreateRequest request,
            @AuthenticationPrincipal UserAuth user
    ) {
        StudyGroupCreateResponse response = studyGroupService.createStudyGroup(request, user.getId());
        return ResponseEntity.created(URI.create("api/study-groups/" + response.getId())).body(response);
    }

    // 스터디 신청
    @PostMapping("/{groupId}/applications")
    public ResponseEntity<StudyGroupApplyResponse> applyStudyGroup(
            @AuthenticationPrincipal UserAuth user,
            @PathVariable Long groupId
    ) {
        StudyGroupApplyResponse response = studyGroupService.applyStudyGroup(user.getId(), groupId);
        URI uri = URI.create("api/study-groups/" + groupId + "/application/" + response.getId());
        return ResponseEntity.created(uri).body(response);
    }

    // 스터디 신청 승인 (리더만)
    @PostMapping("/{groupId}/applications/{applicationId}/approve")
    public ResponseEntity<ApplicationApproveResponse> approveApplication(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserAuth user
    ) {
        ApplicationApproveResponse response = studyGroupService.approveApplication(groupId, applicationId, user.getId());
        return ResponseEntity.ok(response);
    }

    // 스터디 신청 거절 (리더만)
    @PostMapping("/{groupId}/applications/{applicationId}/reject")
    public ResponseEntity<ApplicationApproveResponse> rejectApplication(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserAuth user
    ) {
        studyGroupService.rejectApplication(groupId, applicationId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 스터디 그룹 정보 수정 (리더만)
    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroupUpdateResponse> updateStudyGroup(
            @RequestBody StudyGroupUpdateRequest request,
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth user
    ) {
        StudyGroupUpdateResponse response = studyGroupService.updateStudyGroup(request, groupId, user.getId());
        return ResponseEntity.ok(response);
    }

    // 모집 상태 변경 (리더만)
    @PatchMapping("/{groupId}/status")
    public ResponseEntity<StudyGroupUpdateResponse> updateStudyGroupStatus(
            @RequestBody StudyGroupStatusUpdateRequest request,
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth user
    ) {
        StudyGroupUpdateResponse response = studyGroupService.updateStudyGroupStatus(request, groupId, user.getId());
        return ResponseEntity.ok(response);
    }

    // 스터디 그룹 삭제 (리더만)
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteStudyGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth user
    ) {
        studyGroupService.deleteStudyGroup(groupId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 스터디 신청 취소 (본인만)
    @DeleteMapping("/{groupId}/applications/{applicationId}")
    public ResponseEntity<Void> deleteStudyGroupApplication(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserAuth user
    ) {
        studyGroupService.deleteStudyGroupApplication(groupId, applicationId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 스터디 탈퇴 (본인만)
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveStudyGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth user
    ) {
        studyGroupService.leaveStudyGroup(groupId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 스터디 멤버 강제 탈퇴 (리더만)
    @DeleteMapping("/{groupId}/members/{groupMemberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long groupMemberId,
            @AuthenticationPrincipal UserAuth user
    ) {
        studyGroupService.removeMember(groupId, groupMemberId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
