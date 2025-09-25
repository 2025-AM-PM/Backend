package AM.PM.Homepage.member.refreshtoken.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.refreshtoken.domain.RefreshToken;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.refreshtoken.repository.RefreshTokenRepository;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.CookieProvider;
import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String REFRESH = "refresh";

    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    private final CookieProvider provider;
    private final JwtUtil jwtUtil;

    @Transactional
    public void reissuedAccessToken(Long studentId, HttpServletRequest request, HttpServletResponse response) {
        log.info("[액세스 토큰 재발급 요청] studentId={}", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new CustomException(ErrorCode.NOT_FOUND_STUDENT);
        }

        String refreshToken = extractRefreshToken(request.getCookies());
        validateRefreshToken(refreshToken);

        String username = jwtUtil.getUsername(refreshToken);
        StudentRole role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(studentId, username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(studentId, username, role);

        refreshTokenRepository.deleteByRefreshToken(refreshToken);
        registerRefreshToken(newRefreshToken);
        setResponseHeaders(response, newAccessToken, newRefreshToken);

        log.info("[액세스 토큰 재발급 완료] studentId={}", studentId);
    }

    private String extractRefreshToken(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }
        for (Cookie cookie : cookies) {
            if (REFRESH.equals(cookie.getName())) {
                String token = cookie.getValue();
                if (token == null || token.isBlank()) {
                    throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
                }
                return token;
            }
        }
        throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
    }

    private void validateRefreshToken(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!REFRESH.equals(category)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }
    }

    private void setResponseHeaders(HttpServletResponse response, String newAccessToken, String newRefreshToken) {
        response.setHeader(AUTHORIZATION, newAccessToken);
        response.addCookie(provider.createCookie(REFRESH, newRefreshToken));
    }

    public void registerRefreshToken(String newRefreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .refreshToken(newRefreshToken)
                .expiration(
                        new Date(System.currentTimeMillis() + JwtTokenExpirationTime.refreshExpirationHours).toString())
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
