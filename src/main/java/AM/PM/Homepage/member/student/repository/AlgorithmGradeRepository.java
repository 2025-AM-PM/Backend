package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlgorithmGradeRepository extends JpaRepository<AlgorithmProfile, Long> {
}
