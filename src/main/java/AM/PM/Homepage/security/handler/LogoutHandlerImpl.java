package AM.PM.Homepage.security.handler;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.refreshtoken.repository.RefreshTokenRepository;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutHandlerImpl implements LogoutHandler {

    private final RefreshTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = request.getHeader(AUTHORIZATION);

        if(jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        tokenRepository.deleteByRefreshToken(refreshToken);

        Long expiration = jwtUtil.getExpiration(refreshToken);
        redisUtil.setBlackList(refreshToken, "access", expiration);
    }
}
