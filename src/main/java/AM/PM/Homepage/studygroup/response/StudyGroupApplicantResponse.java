package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyGroupApplicantResponse {

    private Long studentId;
    private String studentNumber;
    private String studentName;
    private StudyGroupApplicationStatus status;

    public static StudyGroupApplicantResponse from(StudyGroupApplication application) {
        Student student = application.getStudent();

        return new StudyGroupApplicantResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getStudentName(),
                application.getStatus()
        );
    }
}