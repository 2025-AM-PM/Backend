package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long>, PollRepositoryCustom {
}
