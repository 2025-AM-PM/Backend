package AM.PM.Homepage.security.jwt;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import AM.PM.Homepage.util.redis.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final RedisUtil redisUtil;

    private final static String JWT_PAYLOAD_CATEGORY = "category";
    private final static String JWT_PAYLOAD_USERNAME = "username";
    private final static String JWT_PAYLOAD_ROLE = "role";
    private final static String JWT_PAYLOAD_ID = "id";
    private final StudentRepository studentRepository;

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey, RedisUtil redisUtil,
                   StudentRepository studentRepository) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisUtil = redisUtil;
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

        StudentRole role = claims.get(JWT_PAYLOAD_ROLE, StudentRole.class);
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
            return !redisUtil.hasKeyBlackList(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateToken(Long studentId, String category, String username, StudentRole role, long expirationTime) {

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
