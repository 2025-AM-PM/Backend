package AM.PM.Homepage.security.filter;

import AM.PM.Homepage.member.student.request.AuthenticationRequest;
import AM.PM.Homepage.security.handler.LoginFailureHandler;
import AM.PM.Homepage.security.handler.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class StudentLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public StudentLoginFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
    }

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

    private static AuthenticationRequest parseLoginRequest(HttpServletRequest request) throws IOException {
        AuthenticationRequest login;

        ObjectMapper objectMapper = new ObjectMapper();
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        login = objectMapper.readValue(messageBody, AuthenticationRequest.class);
        return login;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

    public void setSuccessHandler(LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setFailureHandler(LoginFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

}

