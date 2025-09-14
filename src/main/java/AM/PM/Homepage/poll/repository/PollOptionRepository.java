package AM.PM.Homepage.poll.repository;

import AM.PM.Homepage.poll.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
