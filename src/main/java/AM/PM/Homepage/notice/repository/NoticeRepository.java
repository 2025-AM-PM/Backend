package AM.PM.Homepage.notice.repository;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.entity.NoticeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    boolean existsByTitleAndNoticeType(String title, NoticeType noticeType);

    @Query("SELECT n FROM Notice n ORDER BY n.createdAt DESC NULLS LAST, n.id DESC LIMIT 5")
    List<Notice> findRecentWithLimit();
}