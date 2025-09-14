package AM.PM.Homepage.poll.service;

import AM.PM.Homepage.poll.entity.Poll;
import AM.PM.Homepage.poll.entity.PollOption;
import AM.PM.Homepage.poll.repository.PollOptionRepository;
import AM.PM.Homepage.poll.repository.PollRepository;
import AM.PM.Homepage.poll.repository.PollVoteRepository;
import AM.PM.Homepage.poll.request.PollCreateRequest;
import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PollOptionRepository pollOptionRepository;

    @Transactional(readOnly = true)
    public Page<PollSummaryResponse> searchPoll(PollSearchParam params, Pageable pageable) {
        log.debug("Poll search | param={}, pageable={}", params, pageable);
        return pollRepository.searchByParam(params, pageable);
    }

    @Transactional(readOnly = true)
    public PollDetailResponse getPoll(Long pollId) {
        return null;
    }

    public PollSummaryResponse create(PollCreateRequest request, Long userId) {
        Poll poll = Poll.createFrom(request, userId);
        pollRepository.save(poll);

        request.getOptions()
                .forEach(opt -> poll.addOption(PollOption.create(poll, opt, userId)));

        log.info("Poll create | id={}, title='{}', user id='{}'", poll.getId(), poll.getTitle(), userId);
        return PollSummaryResponse.from(poll);
    }
}
