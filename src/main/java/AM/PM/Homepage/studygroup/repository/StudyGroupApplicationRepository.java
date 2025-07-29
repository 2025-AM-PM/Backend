package AM.PM.Homepage.studygroup.repository;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyGroupApplicationRepository extends JpaRepository<StudyGroupApplication, Long> {

    boolean existsByStudentAndStudyGroup(Student student, StudyGroup studyGroup);

    Optional<StudyGroupApplication> findByIdAndStudyGroupId(Long applicationId, Long groupId);

    @EntityGraph(attributePaths = {"student"})
    List<StudyGroupApplication> findAllByStudyGroupId(Long groupId);

    @EntityGraph(attributePaths = {
            "studyGroup", "studyGroup.leader", "studyGroup.leader.student"
    })
    List<StudyGroupApplication> findAllByStudentId(Long studentId);
}
