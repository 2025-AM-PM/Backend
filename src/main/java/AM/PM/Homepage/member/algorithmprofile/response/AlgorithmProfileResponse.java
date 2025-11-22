package AM.PM.Homepage.member.algorithmprofile.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class AlgorithmProfileResponse {

    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Integer tier;
    private Integer solvedCount;
    private Integer rating;

    @QueryProjection
    public AlgorithmProfileResponse(Long studentId, String studentName, String studentNumber,
                                    Integer tier, Integer solvedCount, Integer rating) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.tier = tier;
        this.solvedCount = solvedCount;
        this.rating = rating;
    }
}
