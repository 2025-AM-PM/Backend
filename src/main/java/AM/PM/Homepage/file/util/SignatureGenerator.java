package AM.PM.Homepage.file.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SignatureGenerator {

    // 1. final로 변경
    private final String secretKey;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // 2. @Value를 생성자 파라미터로 이동
    public SignatureGenerator(@Value("${app.storage.secret-key}") String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            // 주입이 실패하면 즉시 앱 로딩을 멈추게 함
            throw new IllegalArgumentException("app.storage.secret-key가 .env 파일에 없거나 비어있습니다.");
        }
        this.secretKey = secretKey;
    }

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

            // 3. (수정) 생성자에서 주입받은 secretKey 사용
            // 이 시점에는 secretKey가 null이 아님이 100% 보장됨
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);

            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(messageToSign.getBytes(StandardCharsets.UTF_8));

            // 4. Base64 URL-safe, No-Padding 인코딩
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