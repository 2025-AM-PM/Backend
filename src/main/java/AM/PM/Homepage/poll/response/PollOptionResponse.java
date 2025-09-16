package AM.PM.Homepage.poll.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PollOptionResponse {

    private Long id;
    private String label;

    @QueryProjection
    public PollOptionResponse(Long id, String label) {
        this.id = id;
        this.label = label;
    }
}
