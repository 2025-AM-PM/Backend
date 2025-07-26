package AM.PM.Homepage.studygroup.repository;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    @EntityGraph(attributePaths = {"leader", "leader.student"})
    Page<StudyGroup> findAllByStatus(StudyGroupStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"leader", "leader.student"})
    Page<StudyGroup> findAllByTitleContainsIgnoreCaseAndStatus(String title, StudyGroupStatus status, Pageable pageable);
}