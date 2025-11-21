package AM.PM.Homepage.member.student.response;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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

    @QueryProjection
    public StudentResponse(Long id, String studentNumber, String studentName, StudentRole role) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.studentName = studentName;
        this.role = role;
    }
}