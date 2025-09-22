package AM.PM.Homepage.member.student.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolvedAcInformationResponse {

    private String handle;
    private Integer solvedCount;
    private Integer tier;
    private Integer rating;

}
