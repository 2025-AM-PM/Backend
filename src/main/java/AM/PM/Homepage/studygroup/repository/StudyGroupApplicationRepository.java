package AM.PM.Homepage.studygroup.repository;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupApplicationRepository extends JpaRepository<StudyGroupApplication, Long> {

    boolean existsByStudentAndStudyGroup(Student student, StudyGroup studyGroup);

    Optional<StudyGroupApplication> findByIdAndStudyGroup(Long id, StudyGroup studyGroup);
}
