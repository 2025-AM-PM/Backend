package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollOptionResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import AM.PM.Homepage.poll.response.PollVoteDto;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PollRepositoryCustom {

    Page<PollSummaryResponse> searchByParam(PollSearchParam param, Pageable pageable);

    Optional<PollDetailResponse> findPollDetailResponseById(Long pollId);

    List<PollOptionResponse> findPollOptionResponsesByPollId(Long pollId);

    Set<Long> findOptionIdsByPollIdAndUserId(Long pollId, Long userId);

    List<PollVoteDto> findAllVoteResponseByPollId(Long pollId);

    List<PollVoteDto> findAllVoteAnonymousResponseByPollId(Long pollId);
}
