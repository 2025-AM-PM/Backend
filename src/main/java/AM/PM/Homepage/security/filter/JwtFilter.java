package AM.PM.Homepage.security.filter;

import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader(AUTHORIZATION);

        // 토큰 없으면 통과
        if (!hasBearer(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(PREFIX.length());

        // 액세스 토큰 검증
        try {
            jwtUtil.validateToken(token);
        } catch (JwtException e) {
            send401(response, "유효하지 않은 access 토큰");
            return;
        }

        // 2) 카테고리 검증 (access 토큰만 허용)
        String category = jwtUtil.getCategory(token);
        if (!ACCESS_TOKEN.getValue().equals(category)) {
            send401(response, "access 토큰이 아님");
            return;
        }

        // 3) 블랙리스트(로그아웃/강제만료) 확인
        if (jwtUtil.isBlacklistedAccess(token)) {
            send401(response, "토큰이 철회되었습니다");
            return;
        }

        // 4) 유저 로드
        Long id = jwtUtil.getId(token);
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            send401(response, "찾을 수 없는 유저");
            return;
        }
        Student student = studentOpt.get();

        // 5) 시큐리티 컨텍스트 설정
        UserAuth principal = new UserAuth(
                student.getId(),
                student.getStudentNumber(),
                student.getPassword(),
                student.getStudentName(),
                student.getRole()
        );
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private boolean hasBearer(String accessToken) {
        return accessToken != null && accessToken.startsWith(PREFIX);
    }

    private void send401(HttpServletResponse response, String body) throws IOException {
        log.warn(body);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write('"' + body + '"'); // 간단 JSON 문자열 응답
    }
}
