package AM.PM.Homepage.admin.request;

import java.util.List;
import lombok.Data;

@Data
public class SignupApprovalRequest {

    private List<Long> applicationIds;
}
