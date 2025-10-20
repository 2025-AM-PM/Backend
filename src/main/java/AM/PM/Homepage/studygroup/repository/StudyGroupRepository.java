package AM.PM.Homepage.studygroup.repository;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    @EntityGraph(attributePaths = {"leader", "leader.student"})
    Page<StudyGroup> findAllByStatus(StudyGroupStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"leader", "leader.student"})
    Page<StudyGroup> findAllByTitleContainsIgnoreCaseAndStatus(String title, StudyGroupStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"leader.student", "members.student"})
    @Query("""
                select sg
                from StudyGroup sg
                join sg.members sgm
                where sgm.student.id = :userId
            """)
    List<StudyGroup> findAllByUserId(Long userId);

    @Query("SELECT sg FROM StudyGroup sg ORDER BY sg.createdAt DESC NULLS LAST, sg.id DESC LIMIT 5")
    List<StudyGroup> findRecentWithLimit();
}