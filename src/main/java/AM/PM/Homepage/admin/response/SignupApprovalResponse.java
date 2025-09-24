package AM.PM.Homepage.admin.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupApprovalResponse {

    private List<Long> applicationIds;
    private int total;
}
