package AM.PM.Homepage.studygroup.repository;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

    long countByStudyGroup(StudyGroup studyGroup);

    boolean existsByStudentAndStudyGroup(Student student, StudyGroup studyGroup);
}
