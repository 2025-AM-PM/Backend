package AM.PM.Homepage.member.student.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSuccessResponse {

    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String role;
    private Integer studentTier;
}
