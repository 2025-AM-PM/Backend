package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import AM.PM.Homepage.studygroup.entity.StudyGroupRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyGroupMemberResponse {

    private Long id;
    private Long studyGroupId;
    private StudyGroupRole role;
    private Long studentId;
    private String studentNumber;
    private String studentName;

    public static StudyGroupMemberResponse from(StudyGroupMember sgm) {
        Student student = sgm.getStudent();

        return new StudyGroupMemberResponse(
                sgm.getId(),
                sgm.getStudyGroup().getId(),
                sgm.getRole(),
                student.getId(),
                student.getStudentNumber(),
                student.getStudentName()
        );
    }
}
