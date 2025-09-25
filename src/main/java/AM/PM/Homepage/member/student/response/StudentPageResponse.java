package AM.PM.Homepage.member.student.response;

import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentPageResponse {

    private String studentName;
    private SolvedAcInformationResponse solvedAcInformationResponse;

}
