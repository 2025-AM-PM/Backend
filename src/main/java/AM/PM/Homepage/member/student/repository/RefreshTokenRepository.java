package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByStudentIdAndReissueCountLessThan(UUID id, Long count);
}
