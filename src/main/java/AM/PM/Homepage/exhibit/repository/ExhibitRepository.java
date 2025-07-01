package AM.PM.Homepage.exhibit.repository;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitRepository extends JpaRepository<Exhibit, Long> {
}
