package AM.PM.Homepage.poll.response;

import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class PollResultResponse {

    public PollDetailResponse poll;

    public List<PollResultOptionResponse> options;

    private boolean voted;                          // 내가 투표했는지
    private Set<Long> mySelectedOptionIds;          // 내가 선택한 옵션
}