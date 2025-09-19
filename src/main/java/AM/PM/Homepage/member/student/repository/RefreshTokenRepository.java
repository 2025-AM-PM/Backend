package AM.PM.Homepage.member.student.repository;

import AM.PM.Homepage.member.student.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {


    void deleteRefreshTokenByRefreshToken(String refreshToken);

    void deleteById(Long id);


}
