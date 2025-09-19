package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final AlgorithmProfileService algorithmGradeService;
    private final PasswordEncoder bCryptPasswordEncoder;

    // 학생 회원가입
        public Long signup(StudentSignupRequest request) {
            if(studentRepository.existsByStudentNumber(request.getStudentNumber())) {
                throw new IllegalArgumentException("이미 가입된 학번");
            }

            Student student = Student.signup(
                    request.getStudentNumber(),
                    "ROLE_USER",
                    request.getStudentName(),
                    bCryptPasswordEncoder.encode(request.getStudentPassword())
            );

            studentRepository.save(student);

            return student.getId();
        }

        @Transactional
        public void changeStudentPassword(Long studentId, String password) {
            findByStudentId(studentId)
                    .setPassword(bCryptPasswordEncoder.encode(password));
    }

    public boolean checkPasswordMatch(String encodedPassword, PasswordChangeRequest passwordChangeRequest) {

        if (bCryptPasswordEncoder.matches(passwordChangeRequest.getRawCurrentPassword(), encodedPassword)
                && passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getNewPasswordConfirm())) {
            return true;
        }

        throw new RuntimeException("."); // custom Exception
    }

    public Student findByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber).orElseThrow(EntityNotFoundException::new);
    }

    public boolean verificationStudentCode(Long studentId, VerificationCodeRequest request) {
        VerificationCodeResponse verificationCodeResponse = algorithmGradeService.fetchSolvedBio(request.getSolvedAcNickname());
        return Objects.equals(issueVerificationCode(studentId), verificationCodeResponse.getBio());
    }

    public String issueVerificationCode(Long studentId) {
        return studentRepository.findVerificationCodeById(studentId);
    }

    public StudentInformationResponse showStudentInformationForTest(String solvedAcNickname, String studentNumber) {

        SolvedAcInformationResponse solvedAcInformationResponse
                = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);

        return StudentInformationResponse.builder()
                .studentNumber(studentNumber)
                .solvedAcInformationResponse(solvedAcInformationResponse)
                .build();
    }

    @Transactional
    public StudentInformationResponse linkAlgorithmProfileToStudent(Long studentId, String solvedAcNickname) {
        Student student = studentRepository.findById(studentId).orElseThrow(EntityNotFoundException::new);
        SolvedAcInformationResponse solvedAcInformationResponse = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);
        AlgorithmProfile algorithmProfile = AlgorithmProfile.from(solvedAcInformationResponse);

        algorithmGradeService.registerAlgorithmGrade(algorithmProfile);
        student.linkAlgorithmProfile(algorithmProfile);

        return StudentInformationResponse.builder()
                .studentNumber(student.getStudentNumber())
                .solvedAcInformationResponse(solvedAcInformationResponse)
                .build();
    }

    public LoginSuccessResponse loadStudentInfo(Long id) {
        Student byStudentId = findByStudentId(id);

        return LoginSuccessResponse.builder()
                .studentId(id)
                .studentName(byStudentId.getStudentName())
                .studentNumber(byStudentId.getStudentNumber())
                .studentTier(byStudentId.getBaekjoonTier().getTier())
                .build();
    }

    public StudentResponse showStudentInformation(Long id) {
        Student byStudentId = findByStudentId(id);

        return StudentResponse.builder()
                .studentName(byStudentId.getStudentName())
                .studentNumber(byStudentId.getStudentNumber())
                .build();
    }

    public void registerStudent(List<StudentResponse> studentResponses) {
        List<Student> students = Student.from(studentResponses);
        studentRepository.saveAll(students);
    }

    public void deleteStudent(Long id) {
        studentRepository.delete(findByStudentId(id));
    }

    private Student findByStudentId(Long id) {
        return studentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

}
