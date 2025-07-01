package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.repository.RefreshTokenRepository;
import AM.PM.Homepage.security.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;


    public void reissuedAccessToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
            }
        }

        if(refreshToken == null) throw new RuntimeException("refresh token is null"); // custom exception

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
        }

        String category = jwtUtil.getCategory(refreshToken);

        if (!category.equals("refresh")) {
            throw new RuntimeException("token is not Refresh");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, role);

        response.setHeader(AUTHORIZATION, newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
