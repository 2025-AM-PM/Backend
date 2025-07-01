package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.RefreshToken;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.RefreshTokenRepository;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

import static AM.PM.Homepage.util.CookieProvider.createCookie;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    private final JwtUtil jwtUtil;


    public void reissuedAccessToken(Long studentId, HttpServletRequest request, HttpServletResponse response) {

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

        Student student = studentRepository.findById(studentId).orElseThrow(EntityNotFoundException::new);

        deleteRefreshToken(refreshToken);
        registerRefreshToken(newRefreshToken, student);
        setResponseStatus(response, newAccessToken, newRefreshToken);
    }

    private void setResponseStatus(HttpServletResponse response, String newAccessToken, String newRefreshToken) {
        response.setHeader(AUTHORIZATION, newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));
    }

    private void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteRefreshTokenByRefreshToken(refreshToken);
    }

    public void registerRefreshToken(String newRefreshToken, Student student) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .refreshToken(newRefreshToken)
                .expiration(new Date(System.currentTimeMillis() + JwtTokenExpirationTime.refreshExpirationHours).toString())
                .student(student)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }


}
