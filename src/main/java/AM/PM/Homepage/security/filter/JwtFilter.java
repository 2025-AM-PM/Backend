package AM.PM.Homepage.security.filter;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(AUTHORIZATION);

        if (!parseHeaderToken(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            //response body
            sendUnauthorizedResponse(response);
            return;
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        Student student = Student.builder()
                .studentName(username)
                .studentRole(role)
                .build();

        UserAuth userAuth = new UserAuth(student);

        Authentication authToken = new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.print("access token expired");

        //response status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static boolean parseHeaderToken(String accessToken) {
        return accessToken != null && accessToken.startsWith("Bearer ");
    }


}
