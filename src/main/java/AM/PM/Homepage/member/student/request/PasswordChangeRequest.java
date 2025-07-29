package AM.PM.Homepage.member.student.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    private String rawCurrentPassword;
    private String newPassword;
    private String newPasswordConfirm;


}
