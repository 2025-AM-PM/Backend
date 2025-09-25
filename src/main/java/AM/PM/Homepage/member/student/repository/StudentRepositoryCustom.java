package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.response.AllStudentDetailResponse;
import AM.PM.Homepage.member.student.domain.Student;
import java.util.Optional;

public interface StudentRepositoryCustom {

    AllStudentDetailResponse getAllStudentDetailResponse();

    Optional<Student> findByIdWithAlgorithmProfile(Long studentId);
}
