package AM.PM.Homepage.security.handler;

import AM.PM.Homepage.member.student.repository.RefreshTokenRepository;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class LogoutHandlerImpl implements LogoutHandler {

    private final RefreshTokenRepository repository;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public LogoutHandlerImpl(RefreshTokenRepository repository, JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String bearer = request.getHeader(AUTHORIZATION);

        if(jwtUtil.validateToken(bearer)) {
            throw new RuntimeException();
        }

        UserAuth principal = (UserAuth) authentication.getPrincipal();
        repository.deleteById(principal.getId());

        Long expiration = jwtUtil.getExpiration(bearer);
        redisUtil.setBlackList(bearer, "access", expiration);
    }



}
