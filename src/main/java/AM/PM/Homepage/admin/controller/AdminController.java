package AM.PM.Homepage.admin.controller;

import AM.PM.Homepage.admin.request.SignupApprovalRequest;
import AM.PM.Homepage.admin.request.StudentRoleUpdateRequest;
import AM.PM.Homepage.admin.response.AllStudentDetailResponse;
import AM.PM.Homepage.admin.response.SignupApprovalResponse;
import AM.PM.Homepage.admin.response.StudentDetailResponse;
import AM.PM.Homepage.member.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentService studentService;


    // TODO: 어드민 페이지 기능 만들기

    // ----- 회원가입 신청 관리 -----
    // 회원가입 신청 목록 조회

    // 회원가입 신청 (일괄) 승인 (회장단, 관리자만)
    @PostMapping("/students")
    @PreAuthorize("@authz.isAdmin(authentication)")
    public ResponseEntity<SignupApprovalResponse> approveSignup(
            @RequestBody SignupApprovalRequest request
    ) {
        SignupApprovalResponse response = studentService.approveSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 회원가입 신청 거절

    // ----- 유저 관리 -----
    // 전체 유저 조회 (임원 이상만)
    @GetMapping("/students")
    @PreAuthorize("@authz.isStaff(authentication)")
    public ResponseEntity<AllStudentDetailResponse> getAllStudentDetails() {
        AllStudentDetailResponse response = studentService.getAllStudentDetails();
        return ResponseEntity.ok(response);
    }

    // 회원 권한 수정
    @PatchMapping("/students/{studentId}")
    @PreAuthorize("@authz.isAdmin(authentication)")
    public ResponseEntity<StudentDetailResponse> updateStudentRole(
            @RequestBody StudentRoleUpdateRequest request,
            @PathVariable Long studentId
    ) {
        StudentDetailResponse response = studentService.updateRole(request, studentId);
        return ResponseEntity.ok(response);
    }

    // 회원 삭제
    @DeleteMapping("/students/{studentId}")
    @PreAuthorize("@authz.isAdmin(authentication)")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    // 대시보드 관리
    // 로그 조회
    // 게시글, 투표, 공지 등 작성, 수정 삭제
    
}
