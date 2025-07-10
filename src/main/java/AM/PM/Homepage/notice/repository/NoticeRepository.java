package AM.PM.Homepage.notice.repository;

import AM.PM.Homepage.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
