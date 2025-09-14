package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
}
