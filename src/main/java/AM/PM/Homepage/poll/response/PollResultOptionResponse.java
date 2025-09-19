package AM.PM.Homepage.poll.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PollResultOptionResponse {

    private Long id;
    private String label;
    private long count;
    private List<PollVoterResponse> voters;
}
