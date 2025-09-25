package AM.PM.Homepage.member.algorithmprofile.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationCodeRequest {

    @NotNull
    private String solvedAcNickname;

}
