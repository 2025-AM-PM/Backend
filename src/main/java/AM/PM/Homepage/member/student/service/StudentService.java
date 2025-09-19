package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.*;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final AlgorithmProfileService algorithmGradeService;
    private final PasswordEncoder bCryptPasswordEncoder;

    // 학생 회원가입
    public Long signup(StudentSignupRequest request) {
        log.info("[학생 가입 요청] studentNumber={}, name={}", request.getStudentNumber(), request.getStudentName());

        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            log.warn("[학생 가입 실패] 중복 학번: {}", request.getStudentNumber());
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_NUMBER);
        }

        Student student = Student.signup(
                request.getStudentNumber(),
                "ROLE_USER",
                request.getStudentName(),
                bCryptPasswordEncoder.encode(request.getStudentPassword())
        );

        studentRepository.save(student);
        log.info("[학생 가입 완료] studentId={}, studentNumber={}", student.getId(), student.getStudentNumber());
        return student.getId();
    }

    @Transactional
    public void changeStudentPassword(Long studentId, PasswordChangeRequest request) {
        log.info("[비밀번호 변경 요청] studentId={}", studentId);

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            log.warn("[비밀번호 변경 실패] 새 비밀번호 불일치: studentId={}", studentId);
            throw new CustomException(ErrorCode.PASSWORD_NEW_MISMATCH);
        }

        Student student = findByStudentId(studentId);

        if (!bCryptPasswordEncoder.matches(request.getRawCurrentPassword(), student.getPassword())) {
            log.warn("[비밀번호 변경 실패] 현재 비밀번호 불일치: studentId={}", studentId);
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

    @Transactional
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
        Student s = findByStudentId(id);
        return StudentResponse.builder()
                .studentName(s.getStudentName())
                .studentNumber(s.getStudentNumber())
                .build();
    }

    @Transactional
    public void registerStudent(List<StudentResponse> studentResponses) {
        log.info("[학생 일괄 등록 요청] count={}", studentResponses.size());
        List<Student> students = Student.from(studentResponses);
        studentRepository.saveAll(students);
        log.info("[학생 일괄 등록 완료] saved={}", students.size());
    }

    @Transactional
    public void deleteStudent(Long id) {
        log.info("[학생 삭제 요청] studentId={}", id);
        studentRepository.delete(findByStudentId(id));
        log.info("[학생 삭제 완료] studentId={}", id);
    }

    private Student findByStudentId(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));
    }
}
