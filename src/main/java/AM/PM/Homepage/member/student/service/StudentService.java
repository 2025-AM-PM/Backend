package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.request.StudentRoleUpdateRequest;
import AM.PM.Homepage.member.student.response.AllStudentResponse;
import AM.PM.Homepage.member.auth.response.LoginSuccessResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    // 비밀번호 변경
    public void changeStudentPassword(Long studentId, PasswordChangeRequest request) {
        log.info("[비밀번호 변경 요청] studentId={}", studentId);

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_NEW_MISMATCH);
        }

        Student student = findOrThrowById(studentId);

        if (!bCryptPasswordEncoder.matches(request.getRawCurrentPassword(), student.getPassword())) {
            throw new CustomException(ErrorCode.BAD_CREDENTIALS);
        }

        student.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        log.info("[비밀번호 변경 완료] studentId={}", studentId);
    }

    @Transactional(readOnly = true)
    public LoginSuccessResponse loadStudentInfo(Long id) {
        Student student = findOrThrowById(id);
        return LoginSuccessResponse.builder()
                .studentId(id)
                .studentName(student.getStudentName())
                .studentNumber(student.getStudentNumber())
                .studentTier(student.getBaekjoonTier().getTier())
                .build();
    }

    @Transactional(readOnly = true)
    public StudentResponse showStudentInformation(Long id) {
        log.info("[내정보 조회] 조회 시도 studentId={}", id);
        Student student = findOrThrowById(id);
        return StudentResponse.from(student);
    }

    // 전체 학생 상세정보 보기 (임원 이상)
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'SYSTEM_ADMIN')")
    public AllStudentResponse getStudents() {
        log.info("[전체 학생 정보 요청]");
        List<StudentResponse> responses = studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
        return new AllStudentResponse(responses, responses.size());
    }

    // 권한 수정 (관리자만 변경 가능, 관리자 권한 수정 불가)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public StudentResponse updateRole(StudentRoleUpdateRequest request, Long studentId) {
        log.info("[학생 권한 수정 요청] studentId={}, newRole={}", studentId, request.getRole());
        Student student = findOrThrowById(studentId);

        if (student.getRole() == StudentRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN_CHANGE_ROLE, "시스템 관리자의 역할은 수정할 수 없습니다.");
        }

        student.changeRole(request.getRole());
        log.info("[학생 권한 수정 완료] studentId={}, newRole={}", studentId, request.getRole());
        return StudentResponse.from(student);
    }

    // 학생 엔티티 삭제 (관리자만, 관리자 계정 삭제 불가)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN')")
    public void deleteStudent(Long studentId) {
        log.info("[학생 삭제 요청] studentId={}", studentId);
        Student student = findOrThrowById(studentId);

        if (student.getRole() == StudentRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "시스템 관리자 삭제 불가");
        }

        studentRepository.delete(student);
        log.info("[학생 삭제 완료] studentId={}", studentId);
    }

    // ----- 편의 메서드 -----

    // findById.orElseThrow
    private Student findOrThrowById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));
    }
}
