package AM.PM.Homepage.member.student.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCodeResponse {

    private String solvedAcNickname;
    private String bio;

}
