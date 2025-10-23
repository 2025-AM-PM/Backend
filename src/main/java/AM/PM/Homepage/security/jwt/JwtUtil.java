package AM.PM.Homepage.security.jwt;

import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.common.redis.AuthRedisStore;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.UserAuth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private static final String JWT_PAYLOAD_CATEGORY = "category";
    private static final String JWT_PAYLOAD_USERNAME = "username";
    private static final String JWT_PAYLOAD_ROLE = "role";
    private static final String JWT_PAYLOAD_ID = "id";
    private static final String JWT_PAYLOAD_DEVICE = "deviceId"; // refresh용
    private static final String JWT_PAYLOAD_RTI = "rti";         // refresh용 회전 id

    private static final Duration accessTtl = Duration.ofMinutes(30L);
    private static final Duration refreshTtl = Duration.ofDays(14L);

    private final StudentRepository studentRepository;
    private final AuthRedisStore authRedisStore;

    private final SecretKey secretKey;
    private final String issuer;

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey,
                   @Value("${spring.jwt.issuer}") String issuer,
                   StudentRepository studentRepository,
                   AuthRedisStore authRedisStore) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.issuer = issuer;
        this.studentRepository = studentRepository;
        this.authRedisStore = authRedisStore;
    }

    // ======= 토큰 생성 =======

    public String generateAccessToken(Long studentId, String username, StudentRole role) {
        String jti = UUID.randomUUID().toString();
        long expMillis = System.currentTimeMillis() + accessTtl.toMillis();
        return Jwts.builder()
                .subject(studentId.toString())
                .issuer(issuer)
                .id(jti) // JTI
                .claim(JWT_PAYLOAD_CATEGORY, ACCESS_TOKEN.getValue())
                .claim(JWT_PAYLOAD_USERNAME, username)
                .claim(JWT_PAYLOAD_ROLE, role.name())
                .claim(JWT_PAYLOAD_ID, studentId)
                .issuedAt(new Date())
                .expiration(new Date(expMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * deviceId 포함 + 회전을 위한 rti 포함
     */
    public String generateRefreshToken(Long studentId, String username, StudentRole role, String deviceId) {
        String rti = UUID.randomUUID().toString();
        long expMillis = System.currentTimeMillis() + refreshTtl.toMillis();
        return Jwts.builder()
                .subject(studentId.toString())
                .issuer(issuer)
                .id(UUID.randomUUID().toString()) // refresh 자체 jti
                .claim(JWT_PAYLOAD_CATEGORY, REFRESH_TOKEN.getValue())
                .claim(JWT_PAYLOAD_USERNAME, username)
                .claim(JWT_PAYLOAD_ROLE, role.name())
                .claim(JWT_PAYLOAD_ID, studentId)
                .claim(JWT_PAYLOAD_DEVICE, deviceId)
                .claim(JWT_PAYLOAD_RTI, rti)
                .issuedAt(new Date())
                .expiration(new Date(expMillis))
                .signWith(secretKey)
                .compact();
    }

    // ======= 파싱/게터 =======

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String getUsername(String token) {
        return claims(token).get(JWT_PAYLOAD_USERNAME, String.class);
    }

    public StudentRole getRole(String token) {
        return StudentRole.valueOf(claims(token).get(JWT_PAYLOAD_ROLE, String.class));
    }

    public String getCategory(String token) {
        return claims(token).get(JWT_PAYLOAD_CATEGORY, String.class);
    }

    public Long getId(String token) {
        return claims(token).get(JWT_PAYLOAD_ID, Long.class);
    }

    public String getDeviceId(String token) {
        return claims(token).get(JWT_PAYLOAD_DEVICE, String.class);
    }

    public String getRti(String token) {
        return claims(token).get(JWT_PAYLOAD_RTI, String.class);
    }

    public String getJti(String token) {
        return claims(token).getId();
    }

    /**
     * 남은 만료 밀리초
     */
    public Long getExpiration(String token) {
        Date exp = claims(token).getExpiration();
        return exp.getTime() - System.currentTimeMillis();
    }

    /**
     * exp epoch seconds
     */
    public long getExpEpochSec(String token) {
        return claims(token).getExpiration().getTime() / 1000;
    }

    /**
     * 만료 여부 확인(만료면 ExpiredJwtException 터짐)
     */
    public void isExpired(String token) {
        claims(token).getExpiration();
    }

    public void validateToken(String token) {
        isExpired(token);
        claims(token);
    }

    // ======= 인증 객체 생성 =======

    public Authentication getAuthentication(String token) {
        Claims c = claims(token);
        StudentRole role = StudentRole.valueOf(c.get(JWT_PAYLOAD_ROLE, String.class));
        Long id = c.get(JWT_PAYLOAD_ID, Long.class);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        var principal = new UserAuth(
                student.getId(),
                student.getStudentNumber(),
                student.getPassword(),
                student.getStudentName(),
                student.getRole()
        );
        return new UsernamePasswordAuthenticationToken(
                principal, token, List.of(new SimpleGrantedAuthority(role.getAuthority())));
    }

    // ======= 블랙리스트 헬퍼 =======

    public boolean isBlacklistedAccess(String accessToken) {
        String jti = getJti(accessToken);
        return authRedisStore.isBlacklisted(jti);
    }

    /**
     * 액세스 토큰을 즉시 블랙리스트(남은 만료시간만큼 TTL)
     */
    public void blacklistAccess(String accessToken) {
        long ttlSec = Math.max(0, getExpEpochSec(accessToken) - (System.currentTimeMillis() / 1000));
        authRedisStore.blacklist(getJti(accessToken), Duration.ofSeconds(ttlSec));
    }

    public Duration getAccessTtl() {
        return accessTtl;
    }

    public Duration getRefreshTtl() {
        return refreshTtl;
    }
}
