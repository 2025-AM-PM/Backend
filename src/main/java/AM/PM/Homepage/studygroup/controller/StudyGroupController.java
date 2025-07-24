package AM.PM.Homepage.studygroup.controller;

import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.studygroup.request.StudyGroupCreateRequest;
import AM.PM.Homepage.studygroup.response.ApplicationApproveResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupApplyResponse;
import AM.PM.Homepage.studygroup.response.StudyGroupCreateResponse;
import AM.PM.Homepage.studygroup.service.StudyGroupService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    // 새로운 스터디 생성
    @PostMapping
    public ResponseEntity<StudyGroupCreateResponse> createStudyGroup(
            @RequestBody StudyGroupCreateRequest request,
            @AuthenticationPrincipal UserAuth student
    ) {
        StudyGroupCreateResponse response = studyGroupService.createStudyGroup(request, student.getId());
        return ResponseEntity.created(URI.create("api/study-groups/" + response.getId())).body(response);
    }

    // 스터디 신청
    @PostMapping("/{groupId}/applications")
    public ResponseEntity<StudyGroupApplyResponse> applyStudyGroup(
            @AuthenticationPrincipal UserAuth student,
            @PathVariable Long groupId
    ) {
        StudyGroupApplyResponse response = studyGroupService.applyStudyGroup(student.getId(), groupId);
        URI uri = URI.create("api/study-groups/" + groupId + "/application/" + response.getId());
        return ResponseEntity.created(uri).body(response);
    }

    // 스터디 신청 승인
    @PostMapping("/{groupId}/applications/{applicationId}/approve")
    public ResponseEntity<ApplicationApproveResponse> approveApplication(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserAuth student
    ) {
        ApplicationApproveResponse response = studyGroupService.approveApplication(groupId, applicationId, student.getId());
        return ResponseEntity.ok(response);
    }
}
