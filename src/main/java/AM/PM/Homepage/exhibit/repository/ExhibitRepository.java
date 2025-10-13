package AM.PM.Homepage.exhibit.repository;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExhibitRepository extends JpaRepository<Exhibit, Long> {

    @Query("SELECT e FROM Exhibit e ORDER BY e.createdAt DESC NULLS LAST, e.id DESC LIMIT 5")
    List<Exhibit> findRecentWithLimit();
}
