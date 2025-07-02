package AM.PM.Homepage.security.jwt;

import AM.PM.Homepage.util.constant.JwtTokenExpirationTime;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

import org.springframework.stereotype.Component;

import static AM.PM.Homepage.util.constant.JwtTokenType.ACCESS_TOKEN;
import static AM.PM.Homepage.util.constant.JwtTokenType.REFRESH_TOKEN;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    private final static String JWT_PAYLOAD_CATEGORY = "category";
    private final static String JWT_PAYLOAD_USERNAME = "username";
    private final static String JWT_PAYLOAD_ROLE = "role";


    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JWT_PAYLOAD_USERNAME, String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JWT_PAYLOAD_ROLE, String.class);
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JWT_PAYLOAD_CATEGORY, String.class);
    }

    public void isExpired(String token) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public String generateRefreshToken(Long id, String username, String role) {
        return generateToken(id, REFRESH_TOKEN.getValue(), username, role, JwtTokenExpirationTime.refreshExpirationHours);
    }

    public String generateAccessToken(Long id, String username, String role) {
        return generateToken(id, ACCESS_TOKEN.getValue(), username, role, JwtTokenExpirationTime.accessExpirationMinutes);
    }

    private String generateToken(Long id, String category, String username, String role, long expirationTime) {

        return Jwts.builder()
                .subject(id.toString())
                .claim(JWT_PAYLOAD_CATEGORY, category)
                .claim(JWT_PAYLOAD_USERNAME, username)
                .claim(JWT_PAYLOAD_ROLE, role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

}
