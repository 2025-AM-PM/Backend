package AM.PM.Homepage.member.student.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeParameters {

    private String rawCurrentPassword;
    private String encodedPassword;
    private String newPassword;
    private String newPasswordConfirm;


}
