package AM.PM.Homepage.admin.response;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentDetailResponse {
    private Long id;
    private String studentNumber;
    private String studentName;
    private StudentRole role;
    private AlgorithmProfileResponse baekjoon;

    @QueryProjection
    public StudentDetailResponse(Long studentId, String studentNumber, String studentName, StudentRole role,
                                 Long profileId, Integer tier, Integer solvedCount, Integer rating) {
        this.id = studentId;
        this.studentNumber = studentNumber;
        this.studentName = studentName;
        this.role = role;
        this.baekjoon = new AlgorithmProfileResponse(
                profileId,
                tier,
                solvedCount,
                rating
        );
    }
}

