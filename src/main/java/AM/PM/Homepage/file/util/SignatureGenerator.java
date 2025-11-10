package AM.PM.Homepage.file.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SignatureGenerator {
    @Value("${app.storage.secret-key}")
    private String secretKey;
    private static final String HMAC_ALGORITHM = "HmacSHA256"; //

    /**
     * StorageService API 명세와 일치하는 시그니처를 생성합니다.
     *
     * @param httpMethod "PUT" 또는 "GET" (대문자)
     * @param fileId     파일 ID (String 타입, 예: UUID.randomUUID().toString())
     * @param expires    만료 시간 (Unix timestamp in seconds)
     * @return Base64 URL Encoded Signature
     */
    public String generateSignature(String httpMethod, String fileId, long expires) {

        // 1. 서버의 인터셉터와 동일한 형식으로 메시지 생성
        String messageToSign = httpMethod + "\n" + fileId + "\n" + expires;

        try {
            // 2. HmacSHA256 알고리즘으로 서명
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(messageToSign.getBytes(StandardCharsets.UTF_8));

            // 3. Base64 URL-safe, No-Padding 인코딩
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

        } catch (Exception e) {
            // 실제 프로덕션 코드에서는 더 구체적인 예외 처리를 권장합니다.
            throw new RuntimeException("서명 생성에 실패했습니다.", e);
        }
    }

    /**
     * '지금으로부터 N초 후' 만료되는 Unix timestamp(초 단위)를 생성합니다.
     * * @param secondsFromNow 지금으로부터 만료까지의 시간(초)
     * @return long expires timestamp
     */
    public long createExpiresTimestamp(int secondsFromNow) {
        // System.currentTimeMillis()는 밀리초이므로 1000으로 나누어 초 단위로 변경
        return (System.currentTimeMillis() / 1000) + secondsFromNow;
    }
}
