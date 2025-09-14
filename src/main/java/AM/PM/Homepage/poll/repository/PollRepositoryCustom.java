package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PollRepositoryCustom {

    Page<PollSummaryResponse> searchByParam(PollSearchParam param, Pageable pageable);

    Optional<PollDetailResponse> findDetailWithOption(Long pollId, Long userId);
}
