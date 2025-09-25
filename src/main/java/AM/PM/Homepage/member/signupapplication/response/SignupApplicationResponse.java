package AM.PM.Homepage.member.signupapplication.response;

import AM.PM.Homepage.member.signupapplication.domain.SignupApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupApplicationResponse {

    private int total;
    private SignupApplicationStatus status;
}
