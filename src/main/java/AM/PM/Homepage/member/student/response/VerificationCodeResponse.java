package AM.PM.Homepage.member.student.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class VerificationCodeResponse {

    private String solvedAcNickname;
    private String bio;

}
