package AM.PM.Homepage.member.student.response;

import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudentInformationResponse {

    private String studentNumber;
    private SolvedAcInformationResponse solvedAcInformationResponse;

    @Builder
    public StudentInformationResponse(SolvedAcInformationResponse solvedAcInformationResponse, String studentNumber) {
        this.solvedAcInformationResponse = solvedAcInformationResponse;
        this.studentNumber = studentNumber;
    }

}
