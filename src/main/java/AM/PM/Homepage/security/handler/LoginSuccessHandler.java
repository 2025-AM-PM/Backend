package AM.PM.Homepage.security.handler;

import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.student.response.LoginSuccessResponse;
import AM.PM.Homepage.member.student.service.RefreshTokenService;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@AllArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;
    private final AlgorithmGradeRepository algorithmGradeRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException {
        UserAuth principal = (UserAuth) authentication.getPrincipal();

        Long studentId = principal.getId();
        String studentNumber = principal.getStudentNumber();
        String studentName = principal.getStudentName();
        StudentRole role = principal.getRole();
        Integer algorithmTier = algorithmGradeRepository.findTierByStudentId(studentId)
                .orElse(null);

        LoginSuccessResponse successResponse = new LoginSuccessResponse(
                studentId,
                studentNumber,
                studentName,
                role.name(),
                algorithmTier
        );

        String accessToken = jwtUtil.generateAccessToken(studentId, studentNumber, role);
        String refreshToken = jwtUtil.generateRefreshToken(studentId, studentNumber, role);

        refreshTokenService.registerRefreshToken(refreshToken);

        String loginSuccessResponse = objectMapper.writeValueAsString(successResponse);
        setResponseStatus(response, accessToken, refreshToken, loginSuccessResponse);
    }

    private void setResponseStatus(HttpServletResponse response, String accessToken, String refreshToken,
                                   String successResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());
        response.setHeader(AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("Set-Cookie", ResponseCookie
                .from(REFRESH_TOKEN.getValue(), refreshToken)
                .httpOnly(true).secure(true).path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict").build().toString());
        response.getWriter().write(successResponse);
    }
}
