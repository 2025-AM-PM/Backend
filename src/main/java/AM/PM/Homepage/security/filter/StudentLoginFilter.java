package AM.PM.Homepage.security.filter;

import static org.springframework.util.StringUtils.hasText;

import AM.PM.Homepage.member.student.request.AuthenticationRequest;
import AM.PM.Homepage.security.handler.LoginFailureHandler;
import AM.PM.Homepage.security.handler.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

@Slf4j
public class StudentLoginFilter extends UsernamePasswordAuthenticationFilter {

    public StudentLoginFilter(
            AuthenticationManager authenticationManager,
            LoginSuccessHandler successHandler,
            LoginFailureHandler failureHandler
    ) {
        super();
        setFilterProcessesUrl("/api/student/login");
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationServiceException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("허용되지 않은 메서드");
        }

        AuthenticationRequest login;

        try {
            login = parseLoginRequest(request);
        } catch (IOException e) {
            throw new AuthenticationServiceException("올바르지 않은 로그인 요청 방식");
        }

        String studentNumber = login.getStudentNumber();
        String password = login.getStudentPassword();

        if (!hasText(studentNumber) || !hasText(password)) {
            throw new AuthenticationServiceException("아이디 또는 비밀번호가 잘못됨");
        }

        var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(studentNumber, password);
        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }

    private AuthenticationRequest parseLoginRequest(HttpServletRequest request) throws IOException {
        AuthenticationRequest login;

        ObjectMapper objectMapper = new ObjectMapper();
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        login = objectMapper.readValue(messageBody, AuthenticationRequest.class);
        return login;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}

