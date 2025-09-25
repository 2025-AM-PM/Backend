package AM.PM.Homepage.member.student.response;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String studentNumber;
    private String studentName;
    private StudentRole role;

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getStudentName(),
                student.getRole()
        );
    }
}