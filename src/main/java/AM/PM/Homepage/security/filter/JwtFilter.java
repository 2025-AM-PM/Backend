package AM.PM.Homepage.security.filter;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.constant.JwtTokenType;
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

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader(JwtTokenType.ACCESS_TOKEN.getValue());

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("뭐가 문제일까");
        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            //response body
            sendUnauthorizedResponse(response, "access token expired");
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)

        if (!jwtUtil.parseAccessToken(accessToken)) {
            sendUnauthorizedResponse(response, "invalid access token");
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Student student = Student.builder()
                .studentName(username)
                .studentRole(role)
                .build();

        UserAuth userAuth = new UserAuth(student);

        Authentication authToken = new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static void sendUnauthorizedResponse(HttpServletResponse response, String accessTokenExpired) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.print(accessTokenExpired);

        //response status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static boolean parseHeaderToken(String accessToken) {
        return accessToken != null && accessToken.startsWith("Bearer ");
    }


}
