package AM.PM.Homepage.security.filter;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.request.AuthenticationRequest;
import AM.PM.Homepage.member.student.service.RefreshTokenService;
import AM.PM.Homepage.member.student.service.StudentService;
import AM.PM.Homepage.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

import static AM.PM.Homepage.util.CookieProvider.createCookie;
import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;

@Slf4j
@RequiredArgsConstructor
public class StudentLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final StudentService studentService;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        AuthenticationRequest login;

        try {
            login = parseLoginRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String username = login.getStudentNumber();
        String password = login.getStudentPassword();

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);

        setDetails(request, authRequest);
        return authenticationManager.authenticate(authRequest);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String studentNumber = authResult.getName();
        String role = getAuthority(authResult);

        String accessToken = jwtUtil.generateAccessToken(studentNumber, role);
        String refreshToken = jwtUtil.generateRefreshToken(studentNumber, role);

        Student byStudentName = studentService.findByStudentNumber(studentNumber);

        refreshTokenService.registerRefreshToken(refreshToken, byStudentName);
        setResponseStatus(response, accessToken, refreshToken);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
    }

    private static AuthenticationRequest parseLoginRequest(HttpServletRequest request) throws IOException {
        AuthenticationRequest login = new AuthenticationRequest();

        ObjectMapper objectMapper = new ObjectMapper();
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        login = objectMapper.readValue(messageBody, AuthenticationRequest.class);
        return login;
    }

    private static String getAuthority(Authentication authResult) {
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
    }


    private static void setResponseStatus(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(ACCESS_TOKEN.getValue(), accessToken);
        response.addCookie(createCookie(REFRESH_TOKEN.getValue(), refreshToken));
        response.setStatus(HttpStatus.OK.value());
    }

}
