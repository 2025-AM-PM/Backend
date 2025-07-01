package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AlgorithmProfileService algorithmGradeService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


//    public StudentPageResponse;

    @Transactional
    public void changeStudentPassword(String studentNumber, String password) {
        Student student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(EntityNotFoundException::new);
        student.setPassword(bCryptPasswordEncoder.encode(password));
    }


}
