package AM.PM.Homepage.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthRedisStore {

    private final StringRedisTemplate srt;

    private static String rtKey(long userId, String deviceId) { // RT 해시 저장
        return "auth:rt:%d:%s".formatted(userId, deviceId);
    }
    private static String rtSetKey(long userId) {               // 유저의 디바이스 목록
        return "auth:rtkeys:%d".formatted(userId);
    }
    private static String blKey(String jti) {                   // 액세스 블랙리스트
        return "auth:blacklist:%s".formatted(jti);
    }
    private static String banKey(long userId) {                 // 유저 밴
        return "auth:ban:%d".formatted(userId);
    }

    public void blacklist(String jti, Duration ttl) {
        srt.opsForValue().set(blKey(jti), "1", ttl);
    }
    public boolean isBlacklisted(String jti) {
        Boolean ex = srt.hasKey(blKey(jti));
        return ex;
    }

    public void banUser(long userId, Duration ttl) {
        srt.opsForValue().set(banKey(userId), "1", ttl);
    }
    public boolean isBanned(long userId) {
        return srt.hasKey(banKey(userId));
    }

    public void saveRefresh(long userId, String deviceId, String refreshTokenHash, Duration ttl) {
        srt.opsForValue().set(rtKey(userId, deviceId), refreshTokenHash, ttl);
        srt.opsForSet().add(rtSetKey(userId), deviceId);
        srt.expire(rtSetKey(userId), ttl);
    }

    public String getRefreshHash(long userId, String deviceId) {
        return srt.opsForValue().get(rtKey(userId, deviceId));
    }

    public void deleteRefresh(long userId, String deviceId) {
        srt.delete(rtKey(userId, deviceId));
        srt.opsForSet().remove(rtSetKey(userId), deviceId);
    }

    public void deleteAllRefresh(long userId) {
        Set<String> members = srt.opsForSet().members(rtSetKey(userId));
        if (members != null) {
            for (String deviceId : members) deleteRefresh(userId, deviceId);
        }
        srt.delete(rtSetKey(userId));
    }

    /** 평문 RT를 저장하지 않고 해시로 저장(탈취/유출 대비) */
    public static String sha256Base64(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
