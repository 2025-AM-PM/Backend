package AM.PM.Homepage.security.filter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(AUTHORIZATION);

        if (!hasBearer(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(PREFIX.length());

        try {
            jwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            sendResponseWithBody(response, "access 토큰 만료됨");
            return;
        } catch (JwtException e) {
            sendResponseWithBody(response, "유효하지 않은 access 토큰");
            return;
        }

        Long id = jwtUtil.getId(token);
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            sendResponseWithBody(response, "찾을 수 없는 유저");
            return;
        }

        Student student = studentOpt.get();
        UserAuth principal = new UserAuth(
                student.getId(),
                student.getStudentNumber(),
                student.getPassword(),
                student.getStudentName(),
                student.getRole()
        );

        Authentication authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private boolean hasBearer(String accessToken) {
        return accessToken != null && accessToken.startsWith(PREFIX);
    }

    private void sendResponseWithBody(HttpServletResponse response, String body) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write(body);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
