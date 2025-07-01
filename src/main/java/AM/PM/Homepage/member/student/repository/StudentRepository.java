package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String username);

    Optional<Student> findById(Long id);


}
