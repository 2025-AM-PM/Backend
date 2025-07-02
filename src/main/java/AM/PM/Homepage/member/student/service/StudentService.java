package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.response.SolvedAcResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AlgorithmProfileService algorithmGradeService;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void changeStudentPassword(String studentNumber, String password) {
        Student student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(EntityNotFoundException::new);
        student.setPassword(bCryptPasswordEncoder.encode(password));
    }

    public Student findByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void linkAlgorithmProfileToStudent(Long studentId, String solvedAcNickname) {

        Student student = studentRepository.findById(studentId).orElseThrow(EntityNotFoundException::new);
        SolvedAcResponse solvedAcResponse = algorithmGradeService.fetchSolvedAcInformation(solvedAcNickname);
        AlgorithmProfile algorithmProfile = AlgorithmProfile.from(solvedAcResponse, student);

        algorithmGradeService.registerAlgorithmGrade(algorithmProfile);

        student.linkAlgorithmProfile(algorithmProfile);
    }

}
