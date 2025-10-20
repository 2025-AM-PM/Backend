package AM.PM.Homepage.member.algorithmprofile.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlgorithmProfileResponse {

    private Long id;
    private Integer tier;
    private Integer solvedCount;
    private Integer rating;
}
