package AM.PM.Homepage.member.student.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class StudentResponse {

    private String studentNumber;
    private String phoneNumber;
    private String studentName;

    @Builder
    public StudentResponse(String studentNumber, String phoneNumber, String studentName) {
        this.studentNumber = studentNumber;
        this.phoneNumber = phoneNumber;
        this.studentName = studentName;
    }
}
