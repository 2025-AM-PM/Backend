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

    private String studentNumber;
    private Long studentId;
    private Integer studentTier;
    private String studentName;

}
