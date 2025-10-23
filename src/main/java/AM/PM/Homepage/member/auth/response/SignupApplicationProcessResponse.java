package AM.PM.Homepage.member.auth.response;

import AM.PM.Homepage.member.auth.domain.SignupApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupApplicationProcessResponse {

    private int total;
    private SignupApplicationStatus status;
}
