package AM.PM.Homepage.member.student.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AllStudentResponse {

    private List<StudentResponse> students;
    private long totalCount;
}
