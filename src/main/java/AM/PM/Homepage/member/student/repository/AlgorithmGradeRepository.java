package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlgorithmGradeRepository extends JpaRepository<AlgorithmProfile, Long> {

    @Query("select a.id from AlgorithmProfile a where a.id = :id")
    int findByTier(Long id);
}
