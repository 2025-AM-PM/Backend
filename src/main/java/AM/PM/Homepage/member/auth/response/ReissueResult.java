package AM.PM.Homepage.member.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseCookie;

@Getter
@AllArgsConstructor
public class ReissueResult {

    private String accessToken;
    private ResponseCookie refreshCookie;
    private String deviceId;
}