package AM.PM.Homepage.member.student.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticationRequest {

    private String studentNumber;

    private String studentPassword;
}
