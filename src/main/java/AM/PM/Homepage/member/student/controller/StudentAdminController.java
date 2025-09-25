package AM.PM.Homepage.member.student.controller;

import AM.PM.Homepage.member.student.request.StudentRoleUpdateRequest;
import AM.PM.Homepage.member.student.response.AllStudentDetailResponse;
import AM.PM.Homepage.member.student.response.StudentDetailResponse;
import AM.PM.Homepage.member.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/students")
public class StudentAdminController {

    private final StudentService studentService;

    // 전체 유저 조회 (임원 이상만)
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<AllStudentDetailResponse> getAllStudentDetails() {
        AllStudentDetailResponse response = studentService.getAllStudentDetails();
        return ResponseEntity.ok(response);
    }

    // 회원 권한 수정
    @PatchMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN')")
    public ResponseEntity<StudentDetailResponse> updateStudentRole(
            @RequestBody StudentRoleUpdateRequest request,
            @PathVariable Long studentId
    ) {
        StudentDetailResponse response = studentService.updateRole(request, studentId);
        return ResponseEntity.ok(response);
    }

    // 회원 삭제
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    // 대시보드 관리
    // 로그 조회
    // 게시글, 투표, 공지 등 작성, 수정 삭제
    
}
