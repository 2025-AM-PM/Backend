package AM.PM.Homepage.security.jwt;

import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final String issuer;
    private final StudentRepository studentRepository;

    private final static String JWT_PAYLOAD_CATEGORY = "category";
    private final static String JWT_PAYLOAD_USERNAME = "username";
    private final static String JWT_PAYLOAD_ROLE = "role";
    private final static String JWT_PAYLOAD_ID = "id";

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey,
                   @Value("${spring.jwt.issuer}") String issuer,
                   StudentRepository studentRepository) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.issuer = issuer;
        this.studentRepository = studentRepository;
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(JWT_PAYLOAD_USERNAME, String.class);
    }

    public StudentRole getRole(String token) {
        String roleName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(JWT_PAYLOAD_ROLE, String.class);
        return StudentRole.valueOf(roleName);
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(JWT_PAYLOAD_CATEGORY, String.class);
    }

    public Long getId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(JWT_PAYLOAD_ID, Long.class);
    }

    public void isExpired(String token) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public String generateRefreshToken(Long studentId, String username, StudentRole role) {
        return generateToken(studentId, REFRESH_TOKEN.getValue(), username, role,
                JwtTokenExpirationTime.refreshExpirationHours);
    }

    public String generateAccessToken(Long studentId, String username, StudentRole role) {
        return generateToken(studentId, ACCESS_TOKEN.getValue(), username, role,
                JwtTokenExpirationTime.accessExpirationMinutes);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String roleName = claims.get(JWT_PAYLOAD_ROLE, String.class);
        StudentRole role = StudentRole.valueOf(roleName);
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));

        Long id = claims.get(JWT_PAYLOAD_ID, Long.class);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        UserAuth principal = new UserAuth(
                student.getId(),
                student.getStudentNumber(),
                student.getPassword(),
                student.getStudentName(),
                student.getRole()
        );

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Long getExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expirationDate = claims.getExpiration();

        Date now = new Date();

        return expirationDate.getTime() - now.getTime();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String generateToken(Long studentId, String category, String username, StudentRole role,
                                 long expirationTime) {

        return Jwts.builder()
                .subject(studentId.toString())
                .claim(JWT_PAYLOAD_CATEGORY, category)
                .claim(JWT_PAYLOAD_USERNAME, username)
                .claim(JWT_PAYLOAD_ROLE, role.name())
                .claim(JWT_PAYLOAD_ID, studentId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
}
