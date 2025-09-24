package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.admin.request.SignupApprovalRequest;
import AM.PM.Homepage.admin.request.StudentRoleUpdateRequest;
import AM.PM.Homepage.admin.response.AllStudentDetailResponse;
import AM.PM.Homepage.admin.response.SignupApprovalResponse;
import AM.PM.Homepage.admin.response.StudentDetailResponse;
import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.LoginSuccessResponse;
import AM.PM.Homepage.member.student.response.SolvedAcInformationResponse;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.member.student.response.VerificationCodeResponse;
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

    // 학생 회원가입
    public Long signup(StudentSignupRequest request) {
        log.info("[학생 가입 요청] studentNumber={}, name={}", request.getStudentNumber(), request.getStudentName());

        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_NUMBER);
        }

        Student student = Student.signup(
                request.getStudentNumber(),
                request.getStudentName(),
                bCryptPasswordEncoder.encode(request.getStudentPassword())
        );

        studentRepository.save(student);
        log.info("[학생 가입 완료] studentId={}, studentNumber={}", student.getId(), student.getStudentNumber());
        return student.getId();
    }

    public void changeStudentPassword(Long studentId, PasswordChangeRequest request) {
        log.info("[비밀번호 변경 요청] studentId={}", studentId);

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_NEW_MISMATCH);
        }

        Student student = findByStudentId(studentId);

        if (!bCryptPasswordEncoder.matches(request.getRawCurrentPassword(), student.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        student.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        log.info("[비밀번호 변경 완료] studentId={}", studentId);
    }

    // 검증 유틸 (외부에서 사용 중인 경우 유지)
    public boolean checkPasswordMatch(String encodedPassword, PasswordChangeRequest passwordChangeRequest) {
        return bCryptPasswordEncoder.matches(passwordChangeRequest.getRawCurrentPassword(), encodedPassword)
               && passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getNewPasswordConfirm());
    }

    @Transactional(readOnly = true)
    public Student findByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));
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
        Student s = findByStudentId(id);
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
        Student s = findByStudentId(id);
        log.info("[내정보 조회] 조회 시도 studentId={}", id);
        return StudentResponse.builder()
                .studentName(s.getStudentName())
                .studentNumber(s.getStudentNumber())
                .build();
    }

    @PreAuthorize("@authz.isAdmin(authentication)")
    public void deleteStudent(Long studentId) {
        log.info("[학생 삭제 요청] studentId={}", studentId);
        studentRepository.deleteById(studentId);
        log.info("[학생 삭제 완료] studentId={}", studentId);
    }

    private Student findByStudentId(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));
    }

    // 전체 학생 상세정보 보기 (임원급 이상)
    @PreAuthorize("@authz.isStaff(authentication)")
    public AllStudentDetailResponse getAllStudentDetails() {
        log.info("[전체 학생 정보 요청]");
        return studentRepository.getAllStudentDetailResponse();
    }


    // 회원가입 신청 수락 (회장단, 관리자)
    @PreAuthorize("@authz.isAdmin(authentication)")
    public SignupApprovalResponse approveSignup(SignupApprovalRequest request) {
        // TODO : 회원가입 application으로 전환
        return null;
    }

    // 권한 수정 (회장단 이상만, 관리자 권한 수정 불가 )
    @PreAuthorize("@authz.isAdmin(authentication)")
    public StudentDetailResponse updateRole(StudentRoleUpdateRequest request, Long studentId) {
        log.info("[학생 권한 수정 요청] studentId={}, newRole={}", studentId, request.getNewRole());
        Optional<Student> studentOpt = studentRepository.findByIdWithAlgorithmProfile(studentId);
        if (studentOpt.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_STUDENT);
        }
        Student student = studentOpt.get();
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
}
