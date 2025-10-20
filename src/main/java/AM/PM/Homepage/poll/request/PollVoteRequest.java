package AM.PM.Homepage.poll.request;

import java.util.Set;
import lombok.Data;

@Data
public class PollVoteRequest {

    private Set<Long> optionIds;
}
