package AM.PM.Homepage.poll.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PollOptionResponse {

    private Long id;
    private String label;
    private long count; // 투표수
    private boolean selected; // 내가 선택했는지

    @QueryProjection
    public PollOptionResponse(Long id, String label, long count, boolean selected) {
        this.id = id;
        this.label = label;
        this.count = count;
        this.selected = selected;
    }
}
