package AM.PM.Homepage.member.signupapplication.request;

import java.util.List;
import lombok.Data;

@Data
public class SignupApplicationRequest {

    private List<Long> applicationIds;
}
