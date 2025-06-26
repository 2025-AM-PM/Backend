package AM.PM.Homepage.security.filter;

import AM.PM.Homepage.member.student.domain.RefreshToken;
import AM.PM.Homepage.member.student.repository.RefreshTokenRepository;
import AM.PM.Homepage.member.student.request.AuthenticationRequest;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import AM.PM.Homepage.util.constant.JwtTokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

@RequiredArgsConstructor
public class StudentLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private final static int COOKIE_MAX_AGE = 24*60*60;

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

        String name = authResult.getName();
        String role = getAuthority(authResult);

        String accessToken = jwtUtil.generateAccessToken(name, role);
        String refreshToken = jwtUtil.generateRefreshToken(name, role);


        storeRefreshToken(refreshToken);

        setResponseStatus(response, accessToken, refreshToken);
    }



    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(404);
    }

    private static AuthenticationRequest parseLoginRequest(HttpServletRequest request) throws IOException {
        AuthenticationRequest login;

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

    private static Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);

        return cookie;
    }

    private static void setResponseStatus(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(JwtTokenType.ACCESS_TOKEN.getValue(), accessToken);
        response.addCookie(createCookie(JwtTokenType.REFRESH_TOKEN.getValue(), refreshToken));
        response.setStatus(HttpStatus.OK.value());
    }


    private void storeRefreshToken(String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(UUID.randomUUID())
                .expiration(new Date(System.currentTimeMillis() + JwtTokenExpirationTime.refreshExpirationHours).toString())
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }
}
