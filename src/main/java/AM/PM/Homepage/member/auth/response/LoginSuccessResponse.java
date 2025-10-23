package AM.PM.Homepage.member.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginSuccessResponse {

    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String role;
    private Integer studentTier;
}
