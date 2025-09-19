package AM.PM.Homepage.poll.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PollVoteDto {

    private Long id;
    private Long optionId;
    private String label;
    private Long studentId;
    private String studentName;

    @QueryProjection
    public PollVoteDto(Long id,
                       Long optionId,
                       String label,
                       Long studentId,
                       String studentName
    ) {
        this.id = id;
        this.optionId = optionId;
        this.label = label;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    @QueryProjection
    public PollVoteDto(Long id, Long optionId, String label) {
        this.id = id;
        this.optionId = optionId;
        this.label = label;
        this.studentId = null;
        this.studentName = "ANONYMOUS";
    }
}
