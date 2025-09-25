package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.request.StudentRoleUpdateRequest;
import AM.PM.Homepage.member.student.response.AllStudentDetailResponse;
import AM.PM.Homepage.member.student.response.StudentDetailResponse;
import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.algorithmprofile.domain.AlgorithmProfile;
import AM.PM.Homepage.member.algorithmprofile.service.AlgorithmProfileService;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.algorithmprofile.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.LoginSuccessResponse;
import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.member.algorithmprofile.response.VerificationCodeResponse;
import java.util.Objects;
import java.util.Optional;
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
    private final AlgorithmProfileService algorithmGradeService;
    private final PasswordEncoder bCryptPasswordEncoder;

    // 비밀번호 변경
    public void changeStudentPassword(Long studentId, PasswordChangeRequest request) {
        log.info("[비밀번호 변경 요청] studentId={}", studentId);

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_NEW_MISMATCH);
        }

        Student student = findOrThrowById(studentId);

        if (!bCryptPasswordEncoder.matches(request.getRawCurrentPassword(), student.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        student.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        log.info("[비밀번호 변경 완료] studentId={}", studentId);
    }

    @Transactional(readOnly = true)
    public boolean verificationStudentCode(Long studentId, VerificationCodeRequest request) {
        log.info("[Solved.ac 인증코드 검증] studentId={}, nickname={}", studentId, request.getSolvedAcNickname());
        VerificationCodeResponse solved = algorithmGradeService.fetchSolvedBio(request.getSolvedAcNickname());
        boolean result = Objects.equals(issueVerificationCode(studentId), solved.getBio());
        log.info("[Solved.ac 인증코드 결과] studentId={}, result={}", studentId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public String issueVerificationCode(Long studentId) {
        // 저장된 코드가 없을 수 있으므로 null 허용 (검증 로직에서 처리)
        return studentRepository.findVerificationCodeById(studentId);
    }

    @Transactional(readOnly = true)
    public StudentInformationResponse showStudentInformationForTest(String solvedAcNickname, String studentNumber) {
        log.info("[학생/솔브드 테스트 조회] nickname={}, studentNumber={}", solvedAcNickname, studentNumber);
        SolvedAcInformationResponse solved = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);
        return StudentInformationResponse.builder()
                .studentNumber(studentNumber)
                .solvedAcInformationResponse(solved)
                .build();
    }

    public StudentInformationResponse linkAlgorithmProfileToStudent(Long studentId, String solvedAcNickname) {
        log.info("[솔브드 연동 요청] studentId={}, nickname={}", studentId, solvedAcNickname);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        SolvedAcInformationResponse solved = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);
        AlgorithmProfile algorithmProfile = AlgorithmProfile.from(solved);

        algorithmGradeService.registerAlgorithmGrade(algorithmProfile);
        student.linkAlgorithmProfile(algorithmProfile);

        log.info("[솔브드 연동 완료] studentId={}, tier={}", studentId, solved.getTier());
        return StudentInformationResponse.builder()
                .studentNumber(student.getStudentNumber())
                .solvedAcInformationResponse(solved)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginSuccessResponse loadStudentInfo(Long id) {
        Student s = findOrThrowById(id);
        return LoginSuccessResponse.builder()
                .studentId(id)
                .studentName(s.getStudentName())
                .studentNumber(s.getStudentNumber())
                .studentTier(s.getBaekjoonTier().getTier())
                .build();
    }

    @Transactional(readOnly = true)
    public StudentResponse showStudentInformation(Long id) {
        log.info("[내정보 조회] 조회 시도 studentId={}", id);
        Student s = findOrThrowById(id);
        log.info("[내정보 조회] 조회 시도 studentId={}", id);
        return StudentResponse.builder()
                .studentName(s.getStudentName())
                .studentNumber(s.getStudentNumber())
                .build();
    }

    // 전체 학생 상세정보 보기 (임원 이상)
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'SYSTEM_ADMIN')")
    public AllStudentDetailResponse getAllStudentDetails() {
        log.info("[전체 학생 정보 요청]");
        return studentRepository.getAllStudentDetailResponse();
    }

    // 권한 수정 (관리자만 변경 가능, 관리자 권한 수정 불가 )
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public StudentDetailResponse updateRole(StudentRoleUpdateRequest request, Long studentId) {
        log.info("[학생 권한 수정 요청] studentId={}, newRole={}", studentId, request.getNewRole());
        Student student = studentRepository.findByIdWithAlgorithmProfile(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        if (student.getRole() == StudentRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN_CHANGE_ROLE, "시스템 관리자의 역할은 수정할 수 없습니다.");
        }

        student.changeRole(request.getNewRole());
        AlgorithmProfile profile = student.getBaekjoonTier();
        log.info("[학생 권한 수정 완료] studentId={}, newRole={}", studentId, request.getNewRole());
        return new StudentDetailResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getStudentName(),
                student.getRole(),
                profile != null ? profile.getId() : null,
                profile != null ? profile.getTier() : null,
                profile != null ? profile.getSolvedCount() : null,
                profile != null ? profile.getRating() : null
        );
    }

    // 학생 엔티티 삭제 (관리자만, 관리자 계정 삭제 불가)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN')")
    public void deleteStudent(Long studentId) {
        log.info("[학생 삭제 요청] studentId={}", studentId);
        Student student = findOrThrowById(studentId);
        if(student.getRole() == StudentRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "시스템 관리자 삭제 불가");
        }
        log.info("[학생 삭제 완료] studentId={}", studentId);
    }

    // ----- 편의 메서드 -----

    private Student findOrThrowById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));
    }
}
