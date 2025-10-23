package AM.PM.Homepage.member.auth.request;

import java.util.List;
import lombok.Data;

@Data
public class SignupApplicationRequest {

    private List<Long> applicationIds;
}
