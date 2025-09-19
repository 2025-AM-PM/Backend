package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AlgorithmGradeRepository extends JpaRepository<AlgorithmProfile, Long> {

    @Query("select a.tier from AlgorithmProfile a where a.id = :id")
    Optional<Integer> findByTier(Long id);
}
