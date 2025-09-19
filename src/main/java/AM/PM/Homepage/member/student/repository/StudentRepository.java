package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String username);

    Optional<Student> findById(Long id);

    @Query("select s.verificationToken from Student s where s.id = :id")
    String findVerificationCodeById(Long id);

    List<Student> findByVerificationToken(String verificationToken);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByIdAndStudentRole(Long id, String studentRole);
}
