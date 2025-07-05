package AM.PM.Homepage.member.student.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentInformationResponse {

    private String studentNumber;
    private String solvedAcNickname;
    private Integer solvedCount;
    private Integer tier;
    private Integer rating;

}
