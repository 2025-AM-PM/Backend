package AM.PM.Homepage.admin.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AllStudentDetailResponse {

    private List<StudentDetailResponse> students;
    private long totalCount;
}
