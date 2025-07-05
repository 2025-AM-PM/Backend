package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.member.student.response.VerificationCodeResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AlgorithmProfileService algorithmGradeService;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void changeStudentPassword(String studentNumber, String password) {
        Student student = findByStudentNumber(studentNumber);
        student.setPassword(bCryptPasswordEncoder.encode(password));
    }

    public Student findByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber).orElseThrow(EntityNotFoundException::new);
    }

    public boolean verificationStudentCode(Long studentId, VerificationCodeRequest request) {
        VerificationCodeResponse verificationCodeResponse = algorithmGradeService.fetchSolvedBio(request.getStudentName());
        return Objects.equals(issueVerificationCode(studentId), verificationCodeResponse.getBio());
    }

    public String issueVerificationCode(Long studentId) {
        return studentRepository.findVerificationCodeById(studentId);
    }

    public StudentInformationResponse showStudentInformation(String solvedAcNickname, String studentNumber) {

        StudentInformationResponse studentInformationResponse = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);

        studentInformationResponse.setStudentNumber(studentNumber);
        studentInformationResponse.setSolvedAcNickname(solvedAcNickname);

        return studentInformationResponse;
    }

    @Transactional
    public void linkAlgorithmProfileToStudent(Long studentId, String solvedAcNickname) {

        Student student = studentRepository.findById(studentId).orElseThrow(EntityNotFoundException::new);
        StudentInformationResponse studentInformationResponse = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);
        AlgorithmProfile algorithmProfile = AlgorithmProfile.from(studentInformationResponse);

        algorithmGradeService.registerAlgorithmGrade(algorithmProfile);

        student.linkAlgorithmProfile(algorithmProfile);
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
