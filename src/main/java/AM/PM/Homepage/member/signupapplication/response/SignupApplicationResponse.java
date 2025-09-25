package AM.PM.Homepage.member.signupapplication.response;

import AM.PM.Homepage.member.signupapplication.domain.SignupApplication;
import AM.PM.Homepage.member.signupapplication.domain.SignupApplicationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class SignupApplicationResponse {

    private Long id;
    private String studentNumber;
    private String studentName;
    private SignupApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

    public static SignupApplicationResponse from(SignupApplication application) {
        return new SignupApplicationResponse(
                application.getId(),
                application.getStudentNumber(),
                application.getStudentName(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getApprovedAt(),
                application.getRejectedAt()
        );
    }
}
