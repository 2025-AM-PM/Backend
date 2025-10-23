package AM.PM.Homepage.member.auth.repository;

import AM.PM.Homepage.member.auth.domain.SignupApplication;
import AM.PM.Homepage.member.auth.domain.SignupApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupApplicationRepository extends JpaRepository<SignupApplication, Long> {
    List<SignupApplication> findByStatus(SignupApplicationStatus status);

    boolean existsByStudentNumber(String studentNumber);
}
