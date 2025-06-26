package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
