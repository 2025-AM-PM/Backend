package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface AlgorithmGradeRepository extends JpaRepository<AlgorithmProfile, Long> {

    @Query("select a.tier from AlgorithmProfile a where a.id = :studentId")
    Optional<Integer> findTierByStudentId(@Param("studentId") Long studentId);
}
