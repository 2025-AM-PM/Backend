package AM.PM.Homepage.member.refreshtoken.repository;

import AM.PM.Homepage.member.refreshtoken.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    void deleteByRefreshToken(String refreshToken);
    void deleteById(Long studentId);
}
