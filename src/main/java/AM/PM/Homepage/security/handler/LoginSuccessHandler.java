package AM.PM.Homepage.security.handler;

import AM.PM.Homepage.member.student.response.LoginSuccessResponse;
import AM.PM.Homepage.member.student.service.RefreshTokenService;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private static final int COOKIE_MAX_AGE = 24*60*60;

    public LoginSuccessHandler(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        UserAuth principal = (UserAuth) authentication.getPrincipal();

        Long studentId = principal.getId();
        String studentNumber = authentication.getName();
        String role = getAuthority(authentication);
        String studentName = principal.getName();

        LoginSuccessResponse successResponse = initLoginSuccessResponse(studentNumber, studentId, studentName);

        String accessToken = jwtUtil.generateAccessToken(studentId, studentNumber, role);
        String refreshToken = jwtUtil.generateRefreshToken(studentId,studentNumber, role);

        refreshTokenService.registerRefreshToken(refreshToken);

        String loginSuccessResponse = objectMapper.writeValueAsString(successResponse);
        setResponseStatus(response, accessToken, refreshToken, loginSuccessResponse);
    }

    private static LoginSuccessResponse initLoginSuccessResponse(String studentNumber, Long studentId, String studentName) {
        return LoginSuccessResponse.builder()
                .studentNumber(studentNumber)
                .studentId(studentId)
                .studentTier(null)
                .studentName(studentName)
                .build();
    }

    private static String getAuthority(Authentication authResult) {
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
    }

    private void setResponseStatus(HttpServletResponse response, String accessToken, String refreshToken, String successResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(AUTHORIZATION, "Bearer " + accessToken);
        response.addCookie(createCookie(REFRESH_TOKEN.getValue(), refreshToken));
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(successResponse);
    }

    public Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}
