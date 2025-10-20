package AM.PM.Homepage.member.student.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentSignupRequest {

    private String studentNumber;
    private String studentName;
    private String studentPassword;
}
