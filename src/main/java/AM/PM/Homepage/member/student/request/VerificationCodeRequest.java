package AM.PM.Homepage.member.student.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationCodeRequest {

    @NotNull
    private String studentName;

}
