package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.entity.PollVote;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    List<PollVote> findByPollIdAndVoterId(Long pollId, Long voterId);

    void deleteByPollIdAndVoterIdAndOptionIdIn(Long pollId, Long studentId, HashSet<Long> toDel);
}
