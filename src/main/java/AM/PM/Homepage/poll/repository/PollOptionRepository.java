package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.entity.PollOption;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollIdAndIdIn(Long pollId, Collection<Long> ids);
}
