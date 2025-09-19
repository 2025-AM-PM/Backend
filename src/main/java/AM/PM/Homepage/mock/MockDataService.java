package AM.PM.Homepage.mock;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.member.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MockDataService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final AlgorithmGradeRepository algorithmGradeRepository;

    // mock 학생 데이터 100개 생성
    public void mockSignup() {
        for (int i = 1; i <= 100; i++) {
            StudentSignupRequest request = new StudentSignupRequest(
                    "test" + i,
                    "test_" + i,
                    "test" + i
            );
            Long studentId = studentService.signup(request);

            Student student = studentRepository.findById(studentId)
                    .orElseThrow(IllegalArgumentException::new);

            AlgorithmProfile profile = new AlgorithmProfile(null, i % 31, i, i, student);

            algorithmGradeRepository.save(profile);
        }
    }
}
